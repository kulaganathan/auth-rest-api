package com.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MfaVerificationRequest {

    @NotBlank(message = "MFA code is required")
    @Size(min = 6, max = 8, message = "MFA code must be between 6 and 8 characters")
    private String code;

    // Constructors
    public MfaVerificationRequest() {}

    public MfaVerificationRequest(String code) {
        this.code = code;
    }

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "MfaVerificationRequest{" +
                "code='" + code + '\'' +
                '}';
    }
}
