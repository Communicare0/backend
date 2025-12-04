package com.example.backend.service;

import com.example.backend.dto.request.SendMessageRequest;
import com.example.backend.entity.ChatMessage;

import java.util.List;
import java.util.UUID;

public interface ChatMessageService {

    ChatMessage sendMessage(UUID senderId, SendMessageRequest request);

    List<ChatMessage> getMessages(UUID userId, UUID chatRoomId);
}