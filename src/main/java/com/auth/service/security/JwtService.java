package com.auth.service.security;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class JwtService {

    @Value("${app.jwt.access-token-validity:3600}")
    private long accessTokenValidity;

    @Value("${app.jwt.refresh-token-validity:86400}")
    private long refreshTokenValidity;

    @Value("${app.jwt.rsa.private-key-path:classpath:keys/private.pem}")
    private String privateKeyPath;

    @Value("${app.jwt.rsa.public-key-path:classpath:keys/public.pem}")
    private String publicKeyPath;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(userDetails, accessTokenValidity * 1000);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(userDetails, refreshTokenValidity * 1000);
    }

    public String generateToken(UserDetails userDetails, long expiration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(Object::toString)
                .toArray(String[]::new));
        
        return createToken(claims, userDetails.getUsername(), expiration);
    }

    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .setIssuer("auth-server")
                .signWith(getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getPublicKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JWT token", e);
        }
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private PrivateKey getPrivateKey() {
        if (privateKey == null) {
            try {
                // Load private key from classpath
                java.io.InputStream inputStream = getClass().getClassLoader().getResourceAsStream(
                    privateKeyPath.replace("classpath:", "")
                );
                if (inputStream == null) {
                    throw new RuntimeException("Private key not found: " + privateKeyPath);
                }
                
                byte[] keyBytes = inputStream.readAllBytes();
                String keyString = new String(keyBytes)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
                
                byte[] decodedKey = java.util.Base64.getDecoder().decode(keyString);
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                privateKey = keyFactory.generatePrivate(keySpec);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load private key", e);
            }
        }
        return privateKey;
    }

    private PublicKey getPublicKey() {
        if (publicKey == null) {
            try {
                // Load public key from classpath
                java.io.InputStream inputStream = getClass().getClassLoader().getResourceAsStream(
                    publicKeyPath.replace("classpath:", "")
                );
                if (inputStream == null) {
                    throw new RuntimeException("Public key not found: " + publicKeyPath);
                }
                
                byte[] keyBytes = inputStream.readAllBytes();
                String keyString = new String(keyBytes)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
                
                byte[] decodedKey = java.util.Base64.getDecoder().decode(keyString);
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                publicKey = keyFactory.generatePublic(keySpec);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load public key", e);
            }
        }
        return publicKey;
    }

    public Map<String, Object> getTokenClaims(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Map<String, Object> result = new HashMap<>();
            result.put("username", claims.getSubject());
            result.put("roles", claims.get("roles"));
            result.put("issuedAt", claims.getIssuedAt());
            result.put("expiration", claims.getExpiration());
            result.put("issuer", claims.getIssuer());
            return result;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}
