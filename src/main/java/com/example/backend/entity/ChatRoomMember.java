package com.example.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "chat_room_members", schema = "communicare",
  uniqueConstraints = {
    @UniqueConstraint(name="uk_room_user", columnNames = {"chat_room_id","user_id"})
  },
  indexes = {
    @Index(name="ix_crm_room", columnList = "chat_room_id"),
    @Index(name="ix_crm_user", columnList = "user_id")
  })
public class ChatRoomMember {
  @Id
  @Column(name="chat_room_member_id", columnDefinition = "uuid")
  private UUID chatRoomMemberId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name="chat_room_id", nullable=false,
    foreignKey=@ForeignKey(name="fk_crm_room"))
  private ChatRoom chatRoom;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name="user_id", nullable=false,
    foreignKey=@ForeignKey(name="fk_crm_user"))
  private User user;

  @Column(name="last_read_message_id")
  private UUID lastReadMessageId;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  private OffsetDateTime deletedAt;
  // getters/setters ...
}
