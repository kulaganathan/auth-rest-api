package com.auth.service.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;



@Service
public class PasswordService {

    @Value("${app.password.hash.algorithm:bcrypt}")
    private String hashAlgorithm;

    public String hashPassword(String plainPassword) {
        return hashWithBcrypt(plainPassword);
    }

    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        return verifyWithBcrypt(plainPassword, hashedPassword);
    }

    private String hashWithBcrypt(String plainPassword) {
        return org.springframework.security.crypto.bcrypt.BCrypt.hashpw(plainPassword, org.springframework.security.crypto.bcrypt.BCrypt.gensalt(12));
    }

    private boolean verifyWithBcrypt(String plainPassword, String hashedPassword) {
        return org.springframework.security.crypto.bcrypt.BCrypt.checkpw(plainPassword, hashedPassword);
    }

    public boolean isPasswordStrong(String password) {
        // Check minimum length
        if (password.length() < 8) {
            return false;
        }

        // Check for at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }

        // Check for at least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            return false;
        }

        // Check for at least one digit
        if (!password.matches(".*\\d.*")) {
            return false;
        }

        // Check for at least one special character
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            return false;
        }

        return true;
    }
}
