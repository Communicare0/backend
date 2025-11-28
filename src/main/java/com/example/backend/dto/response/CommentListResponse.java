package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentListResponse {
    private UUID postId;
    private int count;                     // 댓글 개수
    private List<CommentResponse> comments; // 댓글 리스트
}
