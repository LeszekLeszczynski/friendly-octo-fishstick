package com.decerto.leszek.sms;

import java.util.List;

class SmsGatewayClient {

    private final ExternalSmsApi externalApi;

    SmsGatewayClient(ExternalSmsApi externalApi) {
        this.externalApi = externalApi;
    }

    // TODO: limit to max 10 concurrent calls
    public void sendSms(String number, String message) {
        externalApi.send(number, message);
    }

    // TODO: send all, but do not exceed the limit
    public void sendBatch(List<SmsRequest> requests) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
