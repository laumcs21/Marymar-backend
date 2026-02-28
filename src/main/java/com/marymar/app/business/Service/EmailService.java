package com.marymar.app.business.Service;

public interface EmailService {
    void send(String to, String code);

    void sendReset(String to, String code);
}
