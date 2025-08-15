package com.auth.controller;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.dto.MfaSetupResponse;
import com.auth.dto.MfaVerificationRequest;
import com.auth.service.MfaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users/mfa")
@Tag(name = "Multi-Factor Authentication", description = "APIs for MFA setup, verification, and management")
public class MfaController {

    @Autowired
    private MfaService mfaService;

    @PostMapping("/setup")
    @Operation(summary = "Setup MFA", description = "Sets up multi-factor authentication for the current user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MfaSetupResponse> setupMfa(Authentication authentication) {
        // In a real implementation, you'd get the user ID from the authentication
        // For now, we'll use a placeholder - this should be implemented properly
        Long userId = 1L; // This should come from the authenticated user
        MfaSetupResponse response = mfaService.setupMfa(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify MFA code", description = "Verifies the MFA code provided by the user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> verifyMfaCode(
            @Valid @RequestBody MfaVerificationRequest request,
            Authentication authentication) {
        // In a real implementation, you'd get the user ID from the authentication
        Long userId = 1L; // This should come from the authenticated user
        boolean verified = mfaService.verifyMfaCode(userId, request.getCode());
        if (verified) {
            return ResponseEntity.ok("MFA verification successful");
        } else {
            return ResponseEntity.badRequest().body("Invalid MFA code");
        }
    }

    @PostMapping("/disable")
    @Operation(summary = "Disable MFA", description = "Disables multi-factor authentication for the current user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> disableMfa(Authentication authentication) {
        // In a real implementation, you'd get the user ID from the authentication
        Long userId = 1L; // This should come from the authenticated user
        mfaService.disableMfa(userId);
        return ResponseEntity.ok("MFA disabled successfully");
    }

    @PostMapping("/regenerate-backup-codes")
    @Operation(summary = "Regenerate backup codes", description = "Generates new backup codes for MFA")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Set<String>> regenerateBackupCodes(Authentication authentication) {
        // In a real implementation, you'd get the user ID from the authentication
        Long userId = 1L; // This should come from the authenticated user
        Set<String> backupCodes = mfaService.regenerateBackupCodes(userId);
        return ResponseEntity.ok(backupCodes);
    }
}
