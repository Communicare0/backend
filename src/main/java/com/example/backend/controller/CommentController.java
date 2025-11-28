package com.example.backend.controller;

import com.example.backend.dto.request.CreateCommentRequest;
import com.example.backend.dto.request.UpdateCommentRequest;
import com.example.backend.dto.response.CommentResponse;
import com.example.backend.entity.Comment;
import com.example.backend.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/comments")
@Tag(name = "Comment", description = "Comment API")
public class CommentController {

    private final CommentService commentService;

    // TODO(hj): 실제 인증된 사용자 ID로 교체
    private final UUID PLACEHOLDER_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/post/{postId}")
    @Operation(
        summary = "해당 게시글의 댓글 목록 조회",
        parameters = {
            @Parameter(
                name = "postId",
                description = "댓글을 조회할 게시글의 UUID",
                required = true
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "댓글 목록 조회 성공",
                content = @Content(schema = @Schema(implementation = CommentResponse.class))
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
        }
    )
    public ResponseEntity<List<CommentResponse>> getCommentsByPost(@PathVariable UUID postId) {
        List<Comment> comments = commentService.findCommentsByPostId(postId);

        List<CommentResponse> response =
                comments.stream()
                        .map(CommentResponse::fromEntity)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(
        summary = "댓글 생성",
        requestBody = @RequestBody(
            description = "postId, content",
            required = true,
            content = @Content(schema = @Schema(implementation = CreateCommentRequest.class))
        ),
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "댓글 생성 성공",
                content = @Content(schema = @Schema(implementation = CommentResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
        }
    )
    public ResponseEntity<CommentResponse> createComment(
            @org.springframework.web.bind.annotation.RequestBody CreateCommentRequest request
    ) {
        try {
            if (request.getPostId() == null || request.getContent() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            Comment created = commentService.createComment(PLACEHOLDER_USER_ID, request);
            CommentResponse response = CommentResponse.fromEntity(created);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{commentId}")
    @Operation(
        summary = "댓글 수정",
        parameters = {
            @Parameter(
                name = "commentId",
                description = "수정할 댓글의 UUID",
                required = true
            )
        },
        requestBody = @RequestBody(
            required = true,
            content = @Content(schema = @Schema(implementation = UpdateCommentRequest.class))
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "댓글 수정 성공",
                content = @Content(schema = @Schema(implementation = CommentResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
        }
    )
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable UUID commentId,
            @org.springframework.web.bind.annotation.RequestBody UpdateCommentRequest request
    ) {
        try {
            if (commentId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            Comment updated = commentService.updateComment(commentId, request);
            if (updated == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            CommentResponse response = CommentResponse.fromEntity(updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{commentId}")
    @Operation(
        summary = "댓글 삭제",
        parameters = {
            @Parameter(
                name = "commentId",
                description = "삭제할 댓글의 UUID",
                required = true
            )
        },
        responses = {
            @ApiResponse(responseCode = "204", description = "댓글 삭제 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
        }
    )
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId) {
        try {
            if (commentId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            commentService.deleteComment(commentId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}