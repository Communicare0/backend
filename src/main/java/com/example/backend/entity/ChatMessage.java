package com.example.backend.entity;

import com.example.backend.entity.enums.*;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_messages", schema = "communicare",
  indexes = {
    @Index(name="ix_msg_room", columnList = "chat_room_id"),
    @Index(name="ix_msg_sender", columnList = "sender_id")
  })
public class ChatMessage {
  @Id
  @Column(name="chat_message_id", columnDefinition = "uuid")
  private UUID chatMessageId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name="chat_room_id", nullable=false,
    foreignKey=@ForeignKey(name="fk_msg_room"))
  private ChatRoom chatRoom;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name="sender_id", nullable=false,
    foreignKey=@ForeignKey(name="fk_msg_sender"))
  private User sender;

  @Column(columnDefinition = "text", nullable = false)
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MessageType messageType = MessageType.TEXT;

  @Column(nullable = false)
  private boolean isTranslated = false;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  private OffsetDateTime deletedAt;
  // getters/setters ...
}
