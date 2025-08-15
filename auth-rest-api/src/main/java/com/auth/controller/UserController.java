package com.auth.controller;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.auth.dto.UserRegistrationRequest;
import com.auth.dto.UserResponse;
import com.auth.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for user registration, profile management, and role assignment")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with email verification")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse user = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Verify email address", description = "Verifies user email using verification token")
    public ResponseEntity<String> verifyEmail(
            @Parameter(description = "Email verification token") @RequestParam String token) {
        boolean verified = userService.verifyEmail(token);
        if (verified) {
            return ResponseEntity.ok("Email verified successfully");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired verification token");
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Returns the authenticated user's profile information")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        UserResponse user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Returns user profile by user ID (admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}/profile")
    @Operation(summary = "Update user profile", description = "Updates user's first and last name")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<UserResponse> updateUserProfile(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "First name") @RequestParam String firstName,
            @Parameter(description = "Last name") @RequestParam String lastName) {
        UserResponse user = userService.updateUserProfile(userId, firstName, lastName);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}/roles")
    @Operation(summary = "Update user roles", description = "Assigns new roles to a user (admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateUserRoles(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Set of role names") @RequestBody Set<String> roleNames) {
        userService.updateUserRoles(userId, roleNames);
        return ResponseEntity.ok("User roles updated successfully");
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user", description = "Deletes a user account (admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PostMapping("/{userId}/lock")
    @Operation(summary = "Lock user account", description = "Locks a user account (admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> lockUser(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        userService.lockUser(userId);
        return ResponseEntity.ok("User account locked successfully");
    }

    @PostMapping("/{userId}/unlock")
    @Operation(summary = "Unlock user account", description = "Unlocks a user account (admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> unlockUser(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        userService.unlockUser(userId);
        return ResponseEntity.ok("User account unlocked successfully");
    }

    @PostMapping("/{userId}/disable")
    @Operation(summary = "Disable user account", description = "Disables a user account (admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> disableUser(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        userService.disableUser(userId);
        return ResponseEntity.ok("User account disabled successfully");
    }

    @PostMapping("/{userId}/enable")
    @Operation(summary = "Enable user account", description = "Enables a user account (admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> enableUser(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        userService.enableUser(userId);
        return ResponseEntity.ok("User account enabled successfully");
    }
}
