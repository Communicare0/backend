package com.example.backend.repository;

import com.example.backend.entity.Friendship;
import com.example.backend.entity.enums.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    // 요청 내가 보낸 것
    List<Friendship> findByRequester_UserIdAndStatusAndDeletedAtIsNull(
            UUID requesterId,
            FriendshipStatus status
    );

    // 요청 내가 받은 것
    List<Friendship> findByAddressee_UserIdAndStatusAndDeletedAtIsNull(
            UUID addresseeId,
            FriendshipStatus status
    );

    // 두 사람 사이에 살아있는 친구 관계가 있는지 (방향 무시)
    @Query("""
        select f from Friendship f
        where f.deletedAt is null
          and (
              (f.requester.userId = :user1 and f.addressee.userId = :user2)
              or
              (f.requester.userId = :user2 and f.addressee.userId = :user1)
          )
        """)
    Optional<Friendship> findBetweenUsers(UUID user1, UUID user2);

    Optional<Friendship> findByFriendshipIdAndDeletedAtIsNull(UUID friendshipId); 
}