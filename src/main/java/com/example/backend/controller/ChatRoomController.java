package com.example.backend.controller;

import com.example.backend.dto.request.CreateChatRoomRequest;
import com.example.backend.dto.response.ChatRoomResponse;
import com.example.backend.entity.ChatRoom;
import com.example.backend.entity.ChatRoomMember;
import com.example.backend.service.ChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/chat/rooms")
@Tag(name = "ChatRoom", description = "Chat Room API")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    public ChatRoomController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    private UUID extractUserId(Authentication authentication) {
    Object principal = authentication.getPrincipal();

    if (principal instanceof UUID uuid) {
        return uuid;
    }

    if (principal instanceof String s) {
        // JWT subject 가 userId 문자열인 경우
        return UUID.fromString(s);
    }

    // 예: CustomUserDetails 를 쓰는 경우
    if (principal instanceof org.springframework.security.core.userdetails.User user) {
        // username 에 email 이나 userId 저장했는지에 따라 처리
        // 예: username = userId 문자열인 경우:
        return UUID.fromString(user.getUsername());
    }

    throw new IllegalStateException("지원하지 않는 Principal 타입: " + principal.getClass());
}

    @PostMapping
    @Operation(
        summary = "채팅방 생성",
        requestBody = @RequestBody(
            required = true,
            content = @Content(schema = @Schema(implementation = CreateChatRoomRequest.class))
        ),
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "채팅방 생성 성공",
                content = @Content(schema = @Schema(implementation = ChatRoomResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
        }
    )
    public ResponseEntity<ChatRoomResponse> createChatRoom(
            Authentication authentication,
            @org.springframework.web.bind.annotation.RequestBody CreateChatRoomRequest request
    ) {
        try {
            if (request == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            UUID userId = extractUserId(authentication);

            // 방 생성
            var room = chatRoomService.createChatRoom(userId, request);

            // 멤버 목록 조회해서 Response DTO로 변환
            List<ChatRoomMember> members = chatRoomService.getMembersByRoomId(room.getChatRoomId());
            ChatRoomResponse response = ChatRoomResponse.fromEntity(room, members);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();  // 디버깅
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            e.printStackTrace();  // 500 에러 확인.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    @Operation(
        summary = "내가 참여중인 채팅방 목록 조회",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "채팅방 목록 조회 성공",
                content = @Content(schema = @Schema(implementation = ChatRoomResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
        }
    )
    public ResponseEntity<List<ChatRoomResponse>> getMyRooms(Authentication authentication) {
        try {
            UUID userId = (UUID) authentication.getPrincipal();

            List<ChatRoom> rooms = chatRoomService.getMyRooms(userId);
            List<ChatRoomResponse> responses = rooms.stream()
                    .map(room -> {
                        List<ChatRoomMember> members = chatRoomService.getMembersByRoomId(room.getChatRoomId());
                        return ChatRoomResponse.fromEntity(room, members);
                    })
                    .toList();

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}