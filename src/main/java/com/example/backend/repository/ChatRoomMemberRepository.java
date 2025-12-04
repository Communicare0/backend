package com.example.backend.repository;

import com.example.backend.entity.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, UUID> {
    
    List<ChatRoomMember> findByChatRoom_ChatRoomId(UUID chatRoomId);

    Optional<ChatRoomMember> findByChatRoom_ChatRoomIdAndUser_UserIdAndDeletedAtIsNull(
            UUID chatRoomId,
            UUID userId
    );

    List<ChatRoomMember> findByUser_UserIdAndDeletedAtIsNull(UUID userId);

    boolean existsByChatRoom_ChatRoomIdAndUser_UserIdAndDeletedAtIsNull(
            UUID chatRoomId,
            UUID userId
    );
}