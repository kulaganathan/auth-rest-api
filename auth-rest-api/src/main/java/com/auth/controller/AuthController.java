package com.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.auth.service.RefreshTokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "APIs for authentication and logout")
public class AuthController {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Logs out the current user and revokes all tokens")
    public ResponseEntity<String> logout(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            refreshTokenService.revokeAllTokensForUser(username);
        }
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/logout/all")
    @Operation(summary = "Logout from all devices", description = "Logs out the current user from all devices")
    public ResponseEntity<String> logoutFromAllDevices(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            refreshTokenService.revokeAllTokensForUser(username);
        }
        return ResponseEntity.ok("Logged out from all devices successfully");
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Refreshes the access token using a valid refresh token")
    public ResponseEntity<String> refreshToken(@RequestParam String refreshToken) {
        try {
            String newAccessToken = refreshTokenService.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(newAccessToken);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid refresh token");
        }
    }
}
