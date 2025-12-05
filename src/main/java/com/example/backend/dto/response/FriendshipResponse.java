package com.example.backend.dto.response;

import com.example.backend.entity.Friendship;
import com.example.backend.entity.enums.FriendshipStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class FriendshipResponse {

    private UUID friendshipId;
    private UUID requesterId;
    private String requesterNickname;
    private UUID addresseeId;
    private String addresseeNickname;
    private FriendshipStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static FriendshipResponse fromEntity(Friendship f) {
        return FriendshipResponse.builder()
                .friendshipId(f.getFriendshipId())
                .requesterId(f.getRequester().getUserId())
                .requesterNickname(f.getRequester().getNickname())
                .addresseeId(f.getAddressee().getUserId())
                .addresseeNickname(f.getAddressee().getNickname())
                .status(f.getStatus())
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }
}