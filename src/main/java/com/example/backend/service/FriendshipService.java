package com.example.backend.service;

import com.example.backend.dto.request.FriendRequestCreateRequest;
import com.example.backend.entity.Friendship;
import com.example.backend.entity.enums.FriendshipStatus;

import java.util.List;
import java.util.UUID;

public interface FriendshipService {

    Friendship sendFriendRequest(UUID requesterId, FriendRequestCreateRequest request);

    List<Friendship> getIncomingRequests(UUID userId, FriendshipStatus status);

    List<Friendship> getOutgoingRequests(UUID userId, FriendshipStatus status);

    Friendship acceptRequest(UUID userId, UUID friendshipId);

    void rejectRequest(UUID userId, UUID friendshipId);
}