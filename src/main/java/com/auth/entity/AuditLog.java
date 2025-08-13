package com.auth.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@EntityListeners(AuditingEntityListener.class)
@Data
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String action;

    private String resourceType;

    private String resourceId;

    private String details;

    private String ipAddress;

    private String userAgent;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Override
    public String toString() {
        return "AuditLog{" +
                "id=" + id +
                ", action='" + action + '\'' +
                ", resourceType='" + resourceType + '\'' +
                ", resourceId='" + resourceId + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
