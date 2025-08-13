package com.auth.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.entity.RefreshToken;
import com.auth.entity.User;
import com.auth.repository.RefreshTokenRepository;
import com.auth.repository.UserRepository;
import com.auth.service.security.JwtService;

@Service
@Transactional
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    public RefreshToken createRefreshToken(User user, String clientId, long validityInSeconds) {
        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(validityInSeconds);

        RefreshToken refreshToken = new RefreshToken(tokenValue, user, clientId, expiresAt);
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByTokenValue(String tokenValue) {
        return refreshTokenRepository.findByTokenValue(tokenValue);
    }

    public String refreshAccessToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenValue(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (!refreshToken.isValid()) {
            throw new RuntimeException("Refresh token is expired or revoked");
        }

        User user = refreshToken.getUser();
        if (!user.getEnabled()) {
            throw new RuntimeException("User account is disabled");
        }

        // Generate new access token
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .disabled(!user.getEnabled())
                .accountExpired(!user.getAccountNonExpired())
                .credentialsExpired(!user.getCredentialsNonExpired())
                .accountLocked(!user.getAccountNonLocked())
                .authorities(user.getRoles().stream()
                        .map(role -> role.getName())
                        .toArray(String[]::new))
                .build();

        String newAccessToken = jwtService.generateAccessToken(userDetails);

        // Revoke the current refresh token
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        return newAccessToken;
    }

    public void revokeRefreshToken(String tokenValue) {
        refreshTokenRepository.findByTokenValue(tokenValue).ifPresent(token -> {
            token.revoke();
            refreshTokenRepository.save(token);
        });
    }

    public void revokeAllTokensForUser(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            List<RefreshToken> tokens = refreshTokenRepository.findValidTokensByUser(user);
            tokens.forEach(token -> {
                token.revoke();
                refreshTokenRepository.save(token);
            });
        });
    }

    public void revokeAllTokensForUserAndClient(String username, String clientId) {
        userRepository.findByUsername(username).ifPresent(user -> {
            refreshTokenRepository.revokeAllTokensForUserAndClient(user, clientId, LocalDateTime.now());
        });
    }

    public void cleanupExpiredTokens() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7); // Keep tokens for 7 days after expiration
        refreshTokenRepository.deleteExpiredTokens(cutoffDate);
    }

    public List<RefreshToken> getValidTokensForUser(String username) {
        return userRepository.findByUsername(username)
                .map(user -> refreshTokenRepository.findValidTokensByUser(user))
                .orElse(List.of());
    }

    public boolean isTokenValid(String tokenValue) {
        return refreshTokenRepository.findByTokenValue(tokenValue)
                .map(RefreshToken::isValid)
                .orElse(false);
    }
}
