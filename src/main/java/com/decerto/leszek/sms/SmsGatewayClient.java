package com.decerto.leszek.sms;

import java.util.List;

class SmsGatewayClient {

    private final ExternalSmsApi externalApi;

    SmsGatewayClient(ExternalSmsApi externalApi) {
        this.externalApi = externalApi;
    }

    // TODO: ogranicz do max 10 równoległych wywołań
    public void sendSms(String number, String message) {
        externalApi.send(number, message);
    }

    // TODO: wyślij wszystkie, ale nie przekrocz limitu
    public void sendBatch(List<SmsRequest> requests) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
