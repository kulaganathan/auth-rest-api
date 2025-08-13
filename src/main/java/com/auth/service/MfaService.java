package com.auth.service;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth.dto.MfaSetupResponse;
import com.auth.entity.User;
import com.auth.repository.UserRepository;

@Service
public class MfaService {

    @Autowired
    private UserRepository userRepository;

    @Value("${app.mfa.issuer:Auth Server}")
    private String issuer;

    @Value("${app.mfa.algorithm:SHA1}")
    private String algorithm;

    @Value("${app.mfa.digits:6}")
    private int digits;

    @Value("${app.mfa.period:30}")
    private int period;

    private final SecureRandom random = new SecureRandom();

    public MfaSetupResponse setupMfa(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate secret key
        String secret = generateSecretKey();
        
        // Generate backup codes
        Set<String> backupCodes = generateBackupCodes();
        
        // Update user
        user.setMfaSecret(secret);
        user.setMfaBackupCodes(backupCodes);
        user.setMfaEnabled(true);
        userRepository.save(user);

        // Generate QR code URL
        String qrCodeUrl = generateQrCodeUrl(user.getUsername(), secret);

        return new MfaSetupResponse(secret, qrCodeUrl, backupCodes);
    }

    public boolean verifyMfaCode(Long userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getMfaEnabled()) {
            throw new RuntimeException("MFA is not enabled for this user");
        }

        // Check if it's a backup code
        if (user.getMfaBackupCodes().contains(code)) {
            // Remove used backup code
            user.getMfaBackupCodes().remove(code);
            userRepository.save(user);
            return true;
        }

        // Verify TOTP code (simplified - in production use a proper TOTP library)
        return verifyTotpCode(user.getMfaSecret(), code);
    }

    public void disableMfa(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        user.setMfaBackupCodes(new HashSet<>());
        userRepository.save(user);
    }

    public Set<String> regenerateBackupCodes(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getMfaEnabled()) {
            throw new RuntimeException("MFA is not enabled for this user");
        }

        Set<String> newBackupCodes = generateBackupCodes();
        user.setMfaBackupCodes(newBackupCodes);
        userRepository.save(user);

        return newBackupCodes;
    }

    private String generateSecretKey() {
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return java.util.Base64.getEncoder().encodeToString(bytes);
    }

    private Set<String> generateBackupCodes() {
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            codes.add(generateBackupCode());
        }
        return codes;
    }

    private String generateBackupCode() {
        int code = 100000 + random.nextInt(900000); // 6-digit code
        return String.valueOf(code);
    }

    private String generateQrCodeUrl(String username, String secret) {
        return String.format(
            "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=%s&digits=%d&period=%d",
            issuer, username, secret, issuer, algorithm, digits, period
        );
    }

    private boolean verifyTotpCode(String secret, String code) {
        // This is a simplified implementation
        // In production, use a proper TOTP library like Google Authenticator
        try {
            long currentTime = System.currentTimeMillis() / 1000;
            long timeStep = period;
            
            // Check current time step and adjacent ones for clock skew
            for (int i = -1; i <= 1; i++) {
                long time = currentTime + (i * timeStep);
                String expectedCode = generateTotpCode(secret, time);
                if (expectedCode.equals(code)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private String generateTotpCode(String secret, long time) {
        // Simplified TOTP generation - in production use a proper library
        try {
            byte[] secretBytes = java.util.Base64.getDecoder().decode(secret);
            byte[] timeBytes = new byte[8];
            for (int i = 7; i >= 0; i--) {
                timeBytes[i] = (byte) (time & 0xFF);
                time >>= 8;
            }
            
            // Use HMAC-SHA1 (simplified)
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA1");
            javax.crypto.spec.SecretKeySpec keySpec = new javax.crypto.spec.SecretKeySpec(secretBytes, "HmacSHA1");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(timeBytes);
            
            // Generate 6-digit code
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24) |
                        ((hash[offset + 1] & 0xFF) << 16) |
                        ((hash[offset + 2] & 0xFF) << 8) |
                        (hash[offset + 3] & 0xFF);
            
            int code = binary % 1000000;
            return String.format("%06d", code);
        } catch (Exception e) {
            return "000000";
        }
    }
}
