package com.example.backend.service;

import com.example.backend.dto.request.FriendRequestCreateRequest;
import com.example.backend.entity.Friendship;
import com.example.backend.entity.User;
import com.example.backend.entity.enums.FriendshipStatus;
import com.example.backend.repository.FriendshipRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class FriendshipServiceImpl implements FriendshipService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    public FriendshipServiceImpl(UserRepository userRepository,
                                 FriendshipRepository friendshipRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    @Override
    public Friendship sendFriendRequest(UUID requesterId, FriendRequestCreateRequest request) {
        if (request.getFriendCode() == null || request.getFriendCode().isBlank()) {
            throw new IllegalArgumentException("friendCode is required");
        }

        User requester = userRepository.getReferenceById(requesterId);
        User addressee = userRepository.findByFriendCode(request.getFriendCode())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 friend_code 입니다."));

        if (requester.getUserId().equals(addressee.getUserId())) {
            throw new IllegalArgumentException("자기 자신에게 친구 요청은 보낼 수 없습니다.");
        }

        // 이미 존재하는 관계(PENDING/ACCEPTED/BLOCKED 등) 있는지 체크
       friendshipRepository.findBetweenUsers(requester.getUserId(), addressee.getUserId())
                .ifPresent(existing -> {
                    if (existing.getDeletedAt() == null) {
                        // 아직 살아있는 관계
                        if (existing.getStatus() == FriendshipStatus.ACCEPTED) {
                            throw new IllegalStateException("이미 친구 상태입니다.");
                        }
                        if (existing.getStatus() == FriendshipStatus.PENDING) {
                            throw new IllegalStateException("이미 친구 요청이 진행 중입니다.");
                        }
                    }
                }); 

        Friendship friendship = new Friendship();
        friendship.setFriendshipId(UUID.randomUUID());
        friendship.setRequester(requester);
        friendship.setAddressee(addressee);
        friendship.setStatus(FriendshipStatus.PENDING);
        OffsetDateTime now = OffsetDateTime.now();
        friendship.setCreatedAt(now);
        friendship.setUpdatedAt(now);

        return friendshipRepository.save(friendship);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Friendship> getIncomingRequests(UUID userId, FriendshipStatus status) {
        return friendshipRepository.findByAddressee_UserIdAndStatusAndDeletedAtIsNull(
                userId,
                status != null ? status : FriendshipStatus.PENDING
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Friendship> getOutgoingRequests(UUID userId, FriendshipStatus status) {
        return friendshipRepository.findByRequester_UserIdAndStatusAndDeletedAtIsNull(
                userId,
                status != null ? status : FriendshipStatus.PENDING
        );
    }

    @Override
    public Friendship acceptRequest(UUID userId, UUID friendshipId) {
        Friendship friendship = friendshipRepository
                .findByFriendshipIdAndDeletedAtIsNull(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 친구 요청입니다."));

        // 내가 받은 요청인지, 상태가 PENDING인지 체크
        if (!friendship.getAddressee().getUserId().equals(userId)) {
            throw new IllegalStateException("본인에게 온 요청만 수락할 수 있습니다.");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 친구 요청입니다.");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship.setUpdatedAt(OffsetDateTime.now());
        return friendshipRepository.save(friendship);
    }

    @Override
    public void rejectRequest(UUID userId, UUID friendshipId) {
        Friendship friendship = friendshipRepository
                .findByFriendshipIdAndDeletedAtIsNull(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 친구 요청입니다."));

        if (!friendship.getAddressee().getUserId().equals(userId)) {
            throw new IllegalStateException("본인에게 온 요청만 거절할 수 있습니다.");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 친구 요청입니다.");
        }

        friendship.setStatus(FriendshipStatus.BLOCKED); // or REJECTED 라는 enum을 새로 추가해도 됨
        friendship.setDeletedAt(OffsetDateTime.now());
        friendship.setUpdatedAt(OffsetDateTime.now());
        friendshipRepository.save(friendship);
    }

    // 친구 목록 조회 (ACCEPTED 상태만)
    @Override
    @Transactional(readOnly = true)
    public List<Friendship> getMyFriends(UUID userId) {
        List<Friendship> result = new ArrayList<>();

        // 내가 requester 인 경우
        result.addAll(friendshipRepository.findByRequester_UserIdAndStatusAndDeletedAtIsNull(
                userId, FriendshipStatus.ACCEPTED
        ));

        // 내가 addressee 인 경우
        result.addAll(friendshipRepository.findByAddressee_UserIdAndStatusAndDeletedAtIsNull(
                userId, FriendshipStatus.ACCEPTED
        ));

        return result;
    }

     // 친구 삭제
    @Override
    public void unfriend(UUID userId, UUID friendshipId) {
        Friendship f = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("친구 관계를 찾을 수 없습니다."));

        if (f.getStatus() != FriendshipStatus.ACCEPTED) {
            throw new IllegalStateException("ACCEPTED 상태의 친구만 삭제할 수 있습니다.");
        }

        if (!f.getRequester().getUserId().equals(userId) &&
            !f.getAddressee().getUserId().equals(userId)) {
            throw new IllegalStateException("이 친구 관계를 삭제할 권한이 없습니다.");
        }

        friendshipRepository.delete(f);
    }

    // 보낸 친구 요청 취소
    @Override
    public void cancelRequest(UUID userId, UUID friendshipId) {
        Friendship f = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("친구 요청을 찾을 수 없습니다."));

        if (!f.getRequester().getUserId().equals(userId)) {
            throw new IllegalStateException("본인이 보낸 친구 요청만 취소할 수 있습니다.");
        }

        if (f.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태의 요청만 취소할 수 있습니다.");
        }

        friendshipRepository.delete(f);
    }
}