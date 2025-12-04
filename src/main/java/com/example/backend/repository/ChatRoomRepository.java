package com.example.backend.repository;

import com.example.backend.entity.ChatRoom;
import com.example.backend.entity.ChatRoomMember;
import com.example.backend.entity.enums.ChatRoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {
   @Query("""
        select distinct cr
        from ChatRoom cr
        join ChatRoomMember m on m.chatRoom = cr
        where m.user.userId = :userId
          and cr.deletedAt is null
          and m.deletedAt is null
        order by cr.updatedAt desc
    """)
    List<ChatRoom> findMyRoomsOrderByUpdatedAtDesc(@Param("userId") UUID userId); 
}