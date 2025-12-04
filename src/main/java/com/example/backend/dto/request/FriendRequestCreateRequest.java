package com.example.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FriendRequestCreateRequest {

    @Schema(description = "상대방 friend_code", example = "ABCD1234")
    private String friendCode;
}