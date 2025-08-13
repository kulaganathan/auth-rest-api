package com.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.auth.dto.MfaSetupResponse;
import com.auth.entity.User;
import com.auth.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class MfaServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MfaService mfaService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setMfaEnabled(false);
    }

    @Test
    void setupMfa_UserExists_ReturnsMfaSetupResponse() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        MfaSetupResponse response = mfaService.setupMfa(1L);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getSecret());
        assertNotNull(response.getQrCodeUrl());
        assertNotNull(response.getBackupCodes());
        assertFalse(response.getBackupCodes().isEmpty());
        assertEquals(10, response.getBackupCodes().size());
    }

    @Test
    void setupMfa_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            mfaService.setupMfa(999L);
        });
    }

    @Test
    void verifyMfaCode_ValidBackupCode_ReturnsTrue() {
        // Arrange
        testUser.setMfaEnabled(true);
        testUser.getMfaBackupCodes().add("123456");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        boolean result = mfaService.verifyMfaCode(1L, "123456");

        // Assert
        assertTrue(result);
    }

    @Test
    void verifyMfaCode_InvalidCode_ReturnsFalse() {
        // Arrange
        testUser.setMfaEnabled(true);
        testUser.setMfaSecret("testSecret");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        boolean result = mfaService.verifyMfaCode(1L, "invalid");

        // Assert
        assertFalse(result);
    }

    @Test
    void verifyMfaCode_MfaNotEnabled_ThrowsException() {
        // Arrange
        testUser.setMfaEnabled(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            mfaService.verifyMfaCode(1L, "123456");
        });
    }
}
