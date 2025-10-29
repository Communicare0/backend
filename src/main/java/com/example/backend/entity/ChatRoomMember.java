package com.example.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "chat_room_members",
  uniqueConstraints = {
    @UniqueConstraint(name="uk_room_user", columnNames = {"chat_room_id","user_id"})
  },
  indexes = {
    @Index(name="ix_crm_room", columnList = "chat_room_id"),
    @Index(name="ix_crm_user", columnList = "user_id")
  })
public class ChatRoomMember {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="chat_room_member_id")
  private Long chatRoomMemberId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name="chat_room_id", nullable=false,
    foreignKey=@ForeignKey(name="fk_crm_room"))
  private ChatRoom chatRoom;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name="user_id", nullable=false,
    foreignKey=@ForeignKey(name="fk_crm_user"))
  private User user;

  @Column(name="last_read_message_id")
  private Long lastReadMessageId;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  private OffsetDateTime deletedAt;
  // getters/setters ...
}
