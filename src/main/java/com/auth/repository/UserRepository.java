package com.auth.repository;

import com.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByEmailVerificationToken(String token);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.emailVerificationExpiresAt < :now")
    List<User> findUnverifiedExpiredUsers(@Param("now") LocalDateTime now);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
    
    @Query("SELECT u FROM User u WHERE u.enabled = true AND u.accountNonLocked = true AND u.accountNonExpired = true AND u.credentialsNonExpired = true")
    List<User> findActiveUsers();
    
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :cutoffDate")
    List<User> findInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);
}
