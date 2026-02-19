package com.marymar.app.business.Service;

public interface PasswordResetService {
    void requestCode(String email);
    void resetPassword(String email, String code, String newPassword);
}
