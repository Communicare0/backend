package com.example.backend.dto.request;

import com.example.backend.entity.enums.ChatRoomType;
import java.util.List;
import java.util.UUID;

public class CreateChatRoomRequest {

    private ChatRoomType chatRoomType; // DIRECT / GROUP
    private String title;              // GROUP 전용 (DIRECT면 null 가능)
    private String photoUrl;           // 선택
    private List<UUID> memberIds;      // 참여자들 (본인 포함 권장)

    public ChatRoomType getChatRoomType() {
        return chatRoomType;
    }

    public void setChatRoomType(ChatRoomType chatRoomType) {
        this.chatRoomType = chatRoomType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public List<UUID> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<UUID> memberIds) {
        this.memberIds = memberIds;
    }
}