package com.decerto.leszek.sms;

interface ExternalSmsApi {

    void send(String number, String message);
}
