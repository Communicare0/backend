package com.example.backend.dto.response;

import com.example.backend.entity.ChatMessage;
import com.example.backend.entity.ChatRoom;
import com.example.backend.entity.ChatRoomMember;
import com.example.backend.entity.enums.ChatRoomType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ChatRoomResponse {

    private UUID chatRoomId;
    private ChatRoomType chatRoomType;
    private String title;
    private String photoUrl;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private List<UUID> memberIds;

    private UUID lastMessageId;
    private String lastMessageContent;
    private UUID lastMessageSenderId;
    private OffsetDateTime lastMessageAt;

    public static ChatRoomResponse fromEntity(ChatRoom room, List<ChatRoomMember> members) {
        ChatRoomResponse resp = new ChatRoomResponse();
        resp.chatRoomId = room.getChatRoomId();
        resp.chatRoomType = room.getChatRoomType();
        resp.title = room.getTitle();
        resp.photoUrl = room.getPhotoUrl();
        resp.createdAt = room.getCreatedAt();
        resp.updatedAt = room.getUpdatedAt();


        ChatMessage lastChat = room.getLastChat();
        if (lastChat != null) {
            resp.lastMessageId = lastChat.getChatMessageId();
            resp.lastMessageContent = lastChat.getContent();
            resp.lastMessageAt = lastChat.getCreatedAt();
            if (lastChat.getSender() != null) {
                resp.lastMessageSenderId = lastChat.getSender().getUserId();
            }
        }

        resp.memberIds = members.stream()
                .map(m -> m.getUser().getUserId())
                .collect(Collectors.toList());
        return resp;
    }

    // getters only 필요하면 추가
    public UUID getChatRoomId() {
        return chatRoomId;
    }

    public ChatRoomType getChatRoomType() {
        return chatRoomType;
    }

    public String getTitle() {
        return title;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public List<UUID> getMemberIds() {
        return memberIds;
    }

    public OffsetDateTime getUpdatedAt() {
    return updatedAt;
    }

    public UUID getLastMessageId() {
        return lastMessageId;
    }

    public String getLastMessageContent() {
        return lastMessageContent;
    }

    public UUID getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    public OffsetDateTime getLastMessageAt() {
        return lastMessageAt;
    }
}