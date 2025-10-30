package com.example.backend.entity;

import com.example.backend.entity.enums.*;
import jakarta.persistence.*;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

/** 2. Friendship */
@Entity
@Table(name = "friendships", schema = "communicare",
  uniqueConstraints = {
    @UniqueConstraint(name="uk_friend_pair", columnNames = {"requester_id","addressee_id"})
  },
  indexes = {
    @Index(name="ix_friend_requester", columnList = "requester_id"),
    @Index(name="ix_friend_addressee", columnList = "addressee_id")
  })
public class Friendship {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="friendship_id")
  private Long friendshipId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name="requester_id", nullable=false,
    foreignKey=@ForeignKey(name="fk_friend_requester"))
  private User requester;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name="addressee_id", nullable=false,
    foreignKey=@ForeignKey(name="fk_friend_addressee"))
  private User addressee;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private FriendshipStatus status = FriendshipStatus.PENDING;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  private OffsetDateTime deletedAt;
  // getters/setters ...
}
