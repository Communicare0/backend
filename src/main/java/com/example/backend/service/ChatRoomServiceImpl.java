package com.example.backend.service;

import com.example.backend.dto.request.CreateChatRoomRequest;
import com.example.backend.entity.ChatRoom;
import com.example.backend.entity.ChatRoomMember;
import com.example.backend.entity.User;
import com.example.backend.entity.enums.ChatRoomStatus;
import com.example.backend.entity.enums.ChatRoomType;
import com.example.backend.repository.ChatRoomMemberRepository;
import com.example.backend.repository.ChatRoomRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;

    public ChatRoomServiceImpl(
            ChatRoomRepository chatRoomRepository,
            ChatRoomMemberRepository chatRoomMemberRepository,
            UserRepository userRepository
    ) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ChatRoom createChatRoom(UUID creatorId, CreateChatRoomRequest request) {
        // 1) 기본 유효성 검사
        if (request == null) {
            throw new IllegalArgumentException("요청 본문이 비어 있습니다.");
        }
        if (request.getChatRoomType() == null) {
            throw new IllegalArgumentException("chatRoomType 은 필수입니다.");
        }
        if (request.getMemberIds() == null || request.getMemberIds().isEmpty()) {
            throw new IllegalArgumentException("memberIds 는 최소 1명 이상이어야 합니다.");
        }

        // creatorId를 멤버 목록에 강제 포함
        Set<UUID> memberSet = new HashSet<>(request.getMemberIds());
        memberSet.add(creatorId);

        // DIRECT / GROUP 별 인원 수 검증
        ChatRoomType type = request.getChatRoomType();
        if (type == ChatRoomType.DIRECT) {
            if (memberSet.size() != 2) {
                throw new IllegalArgumentException("DIRECT 채팅은 정확히 2명의 사용자만 참여 가능합니다.");
            }
        } else { // GROUP
            if (memberSet.size() < 2) {
                throw new IllegalArgumentException("GROUP 채팅은 최소 2명 이상이어야 합니다.");
            }
            if (request.getTitle() == null || request.getTitle().isBlank()) {
                throw new IllegalArgumentException("GROUP 채팅방은 title 이 필요합니다.");
            }
        }

        OffsetDateTime now = OffsetDateTime.now();

        // 2) ChatRoom 엔티티 생성
        ChatRoom room = new ChatRoom();
        room.setChatRoomId(UUID.randomUUID());
        room.setChatRoomType(type);
        room.setTitle(type == ChatRoomType.GROUP ? request.getTitle() : null);
        room.setPhotoUrl(request.getPhotoUrl());
        room.setStatus(ChatRoomStatus.VISIBLE);
        room.setCreatedAt(now);
        room.setUpdatedAt(now);
        room.setDeletedAt(null);
        room.setLastChat(null);

        ChatRoom savedRoom = chatRoomRepository.save(room);

        // 3) 멤버 엔티티 생성
        for (UUID memberId : memberSet) {
            User user = userRepository.getReferenceById(memberId);

            ChatRoomMember member = new ChatRoomMember();
            member.setChatRoomMemberId(UUID.randomUUID());
            member.setChatRoom(savedRoom);
            member.setUser(user);
            member.setLastReadMessageId(null);
            member.setCreatedAt(now);
            member.setUpdatedAt(now);
            member.setDeletedAt(null);

            chatRoomMemberRepository.save(member);
        }

        return savedRoom;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoomMember> getMembersByRoomId(UUID chatRoomId) {
        return chatRoomMemberRepository.findByChatRoom_ChatRoomId(chatRoomId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoom> getMyRooms(UUID userId) {
        // 1) 내가 속한 멤버십 조회
        List<ChatRoomMember> memberships =
                chatRoomMemberRepository.findByUser_UserIdAndDeletedAtIsNull(userId);

        // 2) 채팅방 꺼내고, 삭제/숨김된 방은 제외 + 중복 제거
        return memberships.stream()
                .map(ChatRoomMember::getChatRoom)
                .filter(Objects::nonNull)
                .filter(room ->
                        room.getDeletedAt() == null &&
                        room.getStatus() == ChatRoomStatus.VISIBLE
                )
                .distinct()
                .collect(Collectors.toList());
    }
}