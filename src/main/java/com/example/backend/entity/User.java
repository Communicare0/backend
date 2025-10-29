package com.example.backend.entity;

import com.example.backend.entity.enums.*;
import jakarta.persistence.*;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users",
  uniqueConstraints = {
    @UniqueConstraint(name="uk_user_email", columnNames = "email"),
    @UniqueConstraint(name="uk_user_nickname", columnNames = "nickname"),
    @UniqueConstraint(name="uk_user_friend_code", columnNames = "friend_code")
  })
public class User {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Long userId;

  @Column(length = 255, nullable = false)
  private String email;

  @Column(length = 50, nullable = false)
  private String nickname;

  @Column(length = 100)
  private String department;

  @Column(length = 20)
  private String studentId;

  @Column(length = 50)
  private String nationality;

  @Column(length = 10)
  private String language;

  @Column(length = 2048)
  private String profileImageUrl;

  @Column(name="friend_code", length = 10, nullable = false)
  private String friendCode;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AccountStatus status = AccountStatus.ACTIVE;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  private OffsetDateTime deletedAt;

  // getters/setters ...
}
