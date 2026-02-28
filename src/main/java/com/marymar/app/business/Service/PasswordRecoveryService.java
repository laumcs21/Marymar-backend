package com.marymar.app.business.Service;

public interface PasswordRecoveryService {
    void sendRecoveryEmail(String email);

    void resetPassword(String token, String newPassword);
}
