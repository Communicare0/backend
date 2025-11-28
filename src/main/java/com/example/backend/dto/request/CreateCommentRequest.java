package com.example.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {
    private UUID postId;         // 어떤 게시글에 댓글을 다는지
    private String content;      // 댓글 내용
}
