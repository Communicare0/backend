package com.example.backend.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.example.backend.entity.enums.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "users",
    schema = "communicare",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_user_nickname", columnNames = "nickname"),
        @UniqueConstraint(name = "uk_user_friend_code", columnNames = "friend_code")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(name = "user_id", columnDefinition = "UUID")
    private UUID userId;

    @Column(length = 255, nullable = false)
    private String email;

    @Column(length = 255, nullable = false)
    private String password;

    @Column(length = 50, nullable = false)
    private String nickname;

    @Column(length = 100)
    private String department;

    @Column(length = 20)
    private String studentId;

    @Enumerated(EnumType.STRING)
    private Nationality nationality;

    @Enumerated(EnumType.STRING)
    private PreferredFoodType preferredFoodType;

    @Enumerated(EnumType.STRING)
    private Language language;

    @Column(length = 2048)
    private String profileImageUrl;

    @Column(name = "friend_code", length = 10, nullable = false)
    private String friendCode;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(
        name = "role",
        nullable = false,
        columnDefinition = "communicare.user_role"
    )
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(
        name = "status",
        nullable = false,
        columnDefinition = "communicare.account_status"
    )
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    private OffsetDateTime deletedAt;

    // ---------- JPA lifecycle ----------

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();

        if (userId == null) {
            userId = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        // friend_code NOT NULL + UNIQUE 보장
        if (friendCode == null || friendCode.isBlank()) {
            friendCode = generateFriendCode();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    // ---------- 내부 유틸 ----------

    private String generateFriendCode() {
        String base = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return base.substring(0, 8); // 8자리 코드
    }
}
