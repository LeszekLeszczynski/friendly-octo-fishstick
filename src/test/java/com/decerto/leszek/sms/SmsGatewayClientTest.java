package com.decerto.leszek.sms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class SmsGatewayClientTest {

    private static final int MAX_CONCURRENT = 10;
    private static final int SIMULATED_LATENCY_MS = 200;

    private SpyExternalSmsApi spyApi;
    private SmsGatewayClient client;

    @BeforeEach
    void setUp() {
        spyApi = new SpyExternalSmsApi(SIMULATED_LATENCY_MS);
        client = new SmsGatewayClient(spyApi);
    }

    // ── single sendSms ──────────────────────────────────────────────

    @Test
    void shouldDeliverSingleSms() {
        client.sendSms("+48123456789", "hello");

        assertThat(spyApi.totalCalls()).isEqualTo(1);
    }

    @Test
    void shouldNeverExceedConcurrencyLimitWhenCalledFromManyThreads() throws InterruptedException {
        var threadCount = 50;
        var latch = new CountDownLatch(threadCount);
        var executor = Executors.newFixedThreadPool(threadCount);

        try {
            IntStream.range(0, threadCount).forEach(i ->
                    executor.submit(() -> {
                        try {
                            client.sendSms("+48" + i, "msg-" + i);
                        } finally {
                            latch.countDown();
                        }
                    })
            );
            assertThat(latch.await(30, TimeUnit.SECONDS)).isTrue();
        } finally {
            executor.shutdown();
        }

        assertThat(spyApi.peakConcurrency()).isLessThanOrEqualTo(MAX_CONCURRENT);
        assertThat(spyApi.totalCalls()).isEqualTo(threadCount);
    }

    @Test
    void shouldActuallyUseConcurrency_notJustRunSequentially() throws InterruptedException {
        var threadCount = 20;
        var latch = new CountDownLatch(threadCount);
        var executor = Executors.newFixedThreadPool(threadCount);

        try {
            IntStream.range(0, threadCount).forEach(i ->
                    executor.submit(() -> {
                        try {
                            client.sendSms("+48" + i, "msg-" + i);
                        } finally {
                            latch.countDown();
                        }
                    })
            );
            assertThat(latch.await(30, TimeUnit.SECONDS)).isTrue();
        } finally {
            executor.shutdown();
        }

        assertThat(spyApi.peakConcurrency())
                .as("should use parallelism, not serialize everything")
                .isGreaterThan(1);
    }

    // ── sendBatch ───────────────────────────────────────────────────

    @Test
    void batchShouldDeliverAllMessages() {
        var requests = IntStream.range(0, 25)
                .mapToObj(i -> new SmsRequest("+48" + i, "batch-" + i))
                .toList();

        client.sendBatch(requests);

        assertThat(spyApi.totalCalls()).isEqualTo(25);
    }

    @Test
    void batchShouldNeverExceedConcurrencyLimit() {
        var requests = IntStream.range(0, 50)
                .mapToObj(i -> new SmsRequest("+48" + i, "batch-" + i))
                .toList();

        client.sendBatch(requests);

        assertThat(spyApi.peakConcurrency()).isLessThanOrEqualTo(MAX_CONCURRENT);
    }

    @Test
    void batchShouldUseConcurrency_notJustRunSequentially() {
        var requests = IntStream.range(0, 20)
                .mapToObj(i -> new SmsRequest("+48" + i, "batch-" + i))
                .toList();

        client.sendBatch(requests);

        assertThat(spyApi.peakConcurrency())
                .as("batch should use parallelism")
                .isGreaterThan(1);
    }

    @Test
    void batchShouldHandleEmptyList() {
        client.sendBatch(List.of());

        assertThat(spyApi.totalCalls()).isZero();
    }

    @Test
    void batchShouldHandleSingleElement() {
        client.sendBatch(List.of(new SmsRequest("+48111", "solo")));

        assertThat(spyApi.totalCalls()).isEqualTo(1);
    }

    @Test
    void batchShouldHandleExactlyMaxConcurrentRequests() {
        var requests = IntStream.range(0, MAX_CONCURRENT)
                .mapToObj(i -> new SmsRequest("+48" + i, "msg-" + i))
                .toList();

        client.sendBatch(requests);

        assertThat(spyApi.totalCalls()).isEqualTo(MAX_CONCURRENT);
        assertThat(spyApi.peakConcurrency()).isLessThanOrEqualTo(MAX_CONCURRENT);
    }

    // ── mixed usage ─────────────────────────────────────────────────

    @Test
    void concurrentSendSmsAndBatchShouldRespectGlobalLimit() throws InterruptedException {
        var batchRequests = IntStream.range(0, 30)
                .mapToObj(i -> new SmsRequest("+48" + i, "batch-" + i))
                .toList();

        var directCallCount = 20;
        var latch = new CountDownLatch(1 + directCallCount);
        var executor = Executors.newFixedThreadPool(directCallCount + 1);

        try {
            executor.submit(() -> {
                try {
                    client.sendBatch(batchRequests);
                } finally {
                    latch.countDown();
                }
            });

            IntStream.range(0, directCallCount).forEach(i ->
                    executor.submit(() -> {
                        try {
                            client.sendSms("+49" + i, "direct-" + i);
                        } finally {
                            latch.countDown();
                        }
                    })
            );

            assertThat(latch.await(30, TimeUnit.SECONDS)).isTrue();
        } finally {
            executor.shutdown();
        }

        assertThat(spyApi.peakConcurrency()).isLessThanOrEqualTo(MAX_CONCURRENT);
        assertThat(spyApi.totalCalls()).isEqualTo(30 + directCallCount);
    }

    // ── spy implementation ──────────────────────────────────────────

    private static class SpyExternalSmsApi implements ExternalSmsApi {

        private final int latencyMs;
        private final AtomicInteger currentConcurrency = new AtomicInteger(0);
        private final AtomicInteger peakConcurrency = new AtomicInteger(0);
        private final AtomicInteger totalCalls = new AtomicInteger(0);

        SpyExternalSmsApi(int latencyMs) {
            this.latencyMs = latencyMs;
        }

        @Override
        public void send(String number, String message) {
            var concurrent = currentConcurrency.incrementAndGet();
            peakConcurrency.accumulateAndGet(concurrent, Math::max);
            totalCalls.incrementAndGet();
            try {
                Thread.sleep(latencyMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                currentConcurrency.decrementAndGet();
            }
        }

        int peakConcurrency() {
            return peakConcurrency.get();
        }

        int totalCalls() {
            return totalCalls.get();
        }
    }
}
