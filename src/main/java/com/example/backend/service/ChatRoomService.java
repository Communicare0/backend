package com.example.backend.service;

import com.example.backend.dto.request.CreateChatRoomRequest;
import com.example.backend.entity.ChatRoom;
import com.example.backend.entity.ChatRoomMember;

import java.util.List;
import java.util.UUID;

public interface ChatRoomService {

    ChatRoom createChatRoom(UUID creatorId, CreateChatRoomRequest request);

    List<ChatRoomMember> getMembersByRoomId(UUID chatRoomId);

    List<ChatRoom> getMyRooms(UUID userId);
}