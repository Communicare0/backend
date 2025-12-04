package com.example.backend.dto.response;

import com.example.backend.entity.ChatMessage;
import com.example.backend.entity.enums.MessageType;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class ChatMessageResponse {

    private UUID chatMessageId;
    private UUID chatRoomId;
    private UUID senderId;
    private String senderNickname;
    private String content;
    private MessageType messageType;
    private boolean translated;
    private OffsetDateTime createdAt;

    public static ChatMessageResponse fromEntity(ChatMessage m) {
        return ChatMessageResponse.builder()
                .chatMessageId(m.getChatMessageId())
                .chatRoomId(m.getChatRoom().getChatRoomId())
                .senderId(m.getSender().getUserId())
                .senderNickname(m.getSender().getNickname())
                .content(m.getContent())
                .messageType(m.getMessageType())
                .translated(m.isTranslated())
                .createdAt(m.getCreatedAt())
                .build();
    }
}