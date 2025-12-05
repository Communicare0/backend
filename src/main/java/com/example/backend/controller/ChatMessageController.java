package com.example.backend.controller;

import com.example.backend.dto.request.SendMessageRequest;
import com.example.backend.dto.response.ChatMessageResponse;
import com.example.backend.entity.ChatMessage;
import com.example.backend.service.ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/chat/messages")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    public ChatMessageController(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    @PostMapping
    @Operation(
            summary = "텍스트 메시지 전송 (REST 버전)",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = SendMessageRequest.class))
            )
    )
    public ResponseEntity<ChatMessageResponse> sendMessage(
            Authentication authentication,
            @org.springframework.web.bind.annotation.RequestBody SendMessageRequest request
    ) {
        try {
            UUID userId = (UUID) authentication.getPrincipal();
            ChatMessage m = chatMessageService.sendMessage(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ChatMessageResponse.fromEntity(m));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/room/{chatRoomId}")
    @Operation(summary = "특정 채팅방 메시지 목록 조회")
    @ApiResponse(responseCode = "200", description = "OK")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            Authentication authentication,
            @PathVariable UUID chatRoomId
    ) {
        try {
            UUID userId = (UUID) authentication.getPrincipal();
            List<ChatMessage> list = chatMessageService.getMessages(userId, chatRoomId);
            return ResponseEntity.ok(
                    list.stream().map(ChatMessageResponse::fromEntity).toList()
            );
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}