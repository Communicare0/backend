package com.example.backend.entity;

import com.example.backend.entity.enums.*;
import jakarta.persistence.*;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "chat_rooms", schema = "communicare")
public class ChatRoom {
  @Id
  @Column(name="chat_room_id", columnDefinition = "uuid")
  private UUID chatRoomId;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(
      name = "chat_room_type",
      nullable = false,
      columnDefinition = "communicare.chat_room_type"
  )
  private ChatRoomType chatRoomType;

  @Column(length = 64)
  private String title; // group only

  @Column(length = 255)
  private String photoUrl; // group only

  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Enumerated(EnumType.STRING)
  @Column(
      name = "status",
      nullable = false,
      columnDefinition = "chat_room_status" 
  )
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
