package com.example.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public class SendMessageRequest {

    @Schema(description = "채팅방 ID")
    private UUID chatRoomId;

    @Schema(description = "메시지 내용")
    private String content;
}