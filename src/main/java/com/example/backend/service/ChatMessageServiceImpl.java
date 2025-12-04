package com.example.backend.service;

import com.example.backend.dto.request.SendMessageRequest;
import com.example.backend.entity.ChatMessage;
import com.example.backend.entity.ChatRoom;
import com.example.backend.entity.ChatRoomMember;
import com.example.backend.entity.User;
import com.example.backend.entity.enums.ChatRoomStatus;
import com.example.backend.entity.enums.MessageType;
import com.example.backend.repository.ChatMessageRepository;
import com.example.backend.repository.ChatRoomMemberRepository;
import com.example.backend.repository.ChatRoomRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public ChatMessageServiceImpl(ChatRoomRepository chatRoomRepository,
                                  ChatRoomMemberRepository chatRoomMemberRepository,
                                  ChatMessageRepository chatMessageRepository,
                                  UserRepository userRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ChatMessage sendMessage(UUID senderId, SendMessageRequest request) {
        // 1) 기본 검증
        if (request.getChatRoomId() == null) {
            throw new IllegalArgumentException("채팅방 ID는 필수입니다.");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("메시지 내용은 필수입니다.");
        }

        // 2) 채팅방 조회
        ChatRoom room = chatRoomRepository.findById(request.getChatRoomId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        if (room.getStatus() != ChatRoomStatus.VISIBLE || room.getDeletedAt() != null) {
            throw new IllegalStateException("메시지를 보낼 수 없는 채팅방입니다.");
        }

        // 3) 방 멤버인지 확인 + 멤버 엔티티 가져오기
        ChatRoomMember membership = chatRoomMemberRepository
                .findByChatRoom_ChatRoomIdAndUser_UserIdAndDeletedAtIsNull(room.getChatRoomId(), senderId)
                .orElseThrow(() -> new IllegalStateException("이 채팅방의 멤버가 아닙니다."));

        // 4) 발신자 조회
        User sender = userRepository.getReferenceById(senderId);

        OffsetDateTime now = OffsetDateTime.now();

        // 5) 메시지 생성/저장
        ChatMessage message = new ChatMessage();
        message.setChatMessageId(UUID.randomUUID());
        message.setChatRoom(room);
        message.setSender(sender);
        message.setContent(request.getContent());
        message.setMessageType(MessageType.TEXT);
        message.setTranslated(false);
        message.setCreatedAt(now);
        message.setUpdatedAt(now);

        ChatMessage saved = chatMessageRepository.save(message);

        saved.getSender().getUserId();
        saved.getSender().getNickname();

        // 6) 채팅방 lastChat 갱신
        room.setLastChat(saved);
        room.setUpdatedAt(now);
        chatRoomRepository.save(room);

        // 7) 이 유저의 last_read_message_id 갱신
        membership.setLastReadMessageId(saved.getChatMessageId());
        membership.setUpdatedAt(now);
        chatRoomMemberRepository.save(membership);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessage> getMessages(UUID userId, UUID chatRoomId) {
        // 1) 멤버인지 먼저 확인
        chatRoomMemberRepository
                .findByChatRoom_ChatRoomIdAndUser_UserIdAndDeletedAtIsNull(chatRoomId, userId)
                .orElseThrow(() -> new IllegalStateException("이 채팅방의 멤버가 아닙니다."));

        // 2) 메시지 목록 조회
        return chatMessageRepository
                .findByChatRoom_ChatRoomIdAndDeletedAtIsNullOrderByCreatedAtAsc(chatRoomId);
    }
}