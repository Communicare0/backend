package com.example.backend.controller;

import com.example.backend.dto.request.FriendRequestCreateRequest;
import com.example.backend.dto.response.FriendshipResponse;
import com.example.backend.entity.Friendship;
import com.example.backend.entity.enums.FriendshipStatus;
import com.example.backend.service.FriendshipService;
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
@RequestMapping("/v1/friendships")
public class FriendshipController {

    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @PostMapping("/request")
    @Operation(
            summary = "친구 코드로 친구 요청 보내기",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = FriendRequestCreateRequest.class))
            )
    )
    public ResponseEntity<FriendshipResponse> sendFriendRequest(
            Authentication authentication,
            @org.springframework.web.bind.annotation.RequestBody FriendRequestCreateRequest request
    ) {
        try {
            UUID userId = (UUID) authentication.getPrincipal();
            Friendship friendship = friendshipService.sendFriendRequest(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(FriendshipResponse.fromEntity(friendship));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/incoming")
    @Operation(summary = "받은 친구 요청 목록 조회 (기본: PENDING)")
    public ResponseEntity<List<FriendshipResponse>> getIncoming(
            Authentication authentication,
            @RequestParam(required = false) FriendshipStatus status
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<Friendship> list = friendshipService.getIncomingRequests(userId, status);
        return ResponseEntity.ok(
                list.stream().map(FriendshipResponse::fromEntity).toList()
        );
    }

    @GetMapping("/outgoing")
    @Operation(summary = "내가 보낸 친구 요청 목록 조회 (기본: PENDING)")
    public ResponseEntity<List<FriendshipResponse>> getOutgoing(
            Authentication authentication,
            @RequestParam(required = false) FriendshipStatus status
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<Friendship> list = friendshipService.getOutgoingRequests(userId, status);
        return ResponseEntity.ok(
                list.stream().map(FriendshipResponse::fromEntity).toList()
        );
    }

    @PostMapping("/{friendshipId}/accept")
    @Operation(summary = "받은 친구 요청 수락")
    public ResponseEntity<FriendshipResponse> accept(
            Authentication authentication,
            @PathVariable UUID friendshipId
    ) {
        try {
            UUID userId = (UUID) authentication.getPrincipal();
            Friendship f = friendshipService.acceptRequest(userId, friendshipId);
            return ResponseEntity.ok(FriendshipResponse.fromEntity(f));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{friendshipId}/reject")
    @Operation(summary = "받은 친구 요청 거절")
    public ResponseEntity<Void> reject(
            Authentication authentication,
            @PathVariable UUID friendshipId
    ) {
        try {
            UUID userId = (UUID) authentication.getPrincipal();
            friendshipService.rejectRequest(userId, friendshipId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 구 목록 조회 
    @GetMapping("/my")
    @Operation(summary = "내 친구 목록 조회")
    public ResponseEntity<List<FriendshipResponse>> getMyFriends(
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<Friendship> list = friendshipService.getMyFriends(userId);
        return ResponseEntity.ok(
                list.stream().map(FriendshipResponse::fromEntity).toList()
        );
    }

    // 친구 삭제
    @DeleteMapping("/{friendshipId}")
    @Operation(summary = "친구 삭제")
    public ResponseEntity<Void> unfriend(
            Authentication authentication,
            @PathVariable UUID friendshipId
    ) {
        try {
            UUID userId = (UUID) authentication.getPrincipal();
            friendshipService.unfriend(userId, friendshipId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 친구 요청 취소
    @PostMapping("/{friendshipId}/cancel")
    @Operation(summary = "내가 보낸 친구 요청 취소")
    public ResponseEntity<Void> cancelRequest(
            Authentication authentication,
            @PathVariable UUID friendshipId
    ) {
        try {
            UUID userId = (UUID) authentication.getPrincipal();
            friendshipService.cancelRequest(userId, friendshipId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}