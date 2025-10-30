package com.example.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "notifications", schema = "communicare",
  indexes = {
    @Index(name="ix_notification_receiver", columnList = "receiver_id")
  })
public class Notification {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="notification_id")
  private Long notificationId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name="receiver_id", nullable=false,
    foreignKey=@ForeignKey(name="fk_notification_receiver"))
  private User receiver;

  @Column(length = 256, nullable = false)
  private String content;

  @Column(length = 2048)
  private String redirectUrl;

  @Column(nullable = false)
  private boolean isRead = false;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  @Column(nullable = false)
  private OffsetDateTime updatedAt;
  // getters/setters ...
}
