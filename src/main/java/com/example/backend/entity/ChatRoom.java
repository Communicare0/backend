package com.example.backend.entity;

import com.example.backend.entity.enums.*;
import jakarta.persistence.*;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_rooms")
public class ChatRoom {
  @Id
  @Column(name="chat_room_id", columnDefinition = "uuid")
  private UUID chatRoomId;

  @Enumerated(EnumType.STRING)
  @Column(name="chat_room_type", nullable=false)
  private ChatRoomType chatRoomType;

  @Column(length = 64)
  private String title; // group only

  @Column(length = 255)
  private String photoUrl; // group only

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ChatRoomStatus status = ChatRoomStatus.VISIBLE;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name="last_chat_id", foreignKey=@ForeignKey(name="fk_chatroom_last_chat"))
  private ChatMessage lastChat; // BIGINT FK

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  private OffsetDateTime deletedAt;
  // getters/setters ...
}
