package com.example.backend.repository;

import com.example.backend.entity.ChatMessage;
import com.example.backend.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    List<ChatMessage> findByChatRoom_ChatRoomIdAndDeletedAtIsNullOrderByCreatedAtAsc(UUID chatRoomId);

    Optional<ChatMessage> findTop1ByChatRoom_ChatRoomIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID chatRoomId);
}