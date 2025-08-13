package com.auth.dto;

import java.util.Set;

public class MfaSetupResponse {

    private String secret;
    private String qrCodeUrl;
    private Set<String> backupCodes;
    private String message;

    // Constructors
    public MfaSetupResponse() {}

    public MfaSetupResponse(String secret, String qrCodeUrl, Set<String> backupCodes) {
        this.secret = secret;
        this.qrCodeUrl = qrCodeUrl;
        this.backupCodes = backupCodes;
        this.message = "MFA setup completed successfully. Please scan the QR code with your authenticator app.";
    }

    // Getters and Setters
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public Set<String> getBackupCodes() {
        return backupCodes;
    }

    public void setBackupCodes(Set<String> backupCodes) {
        this.backupCodes = backupCodes;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "MfaSetupResponse{" +
                "secret='" + secret + '\'' +
                ", qrCodeUrl='" + qrCodeUrl + '\'' +
                ", backupCodes=" + backupCodes +
                ", message='" + message + '\'' +
                '}';
    }
}
