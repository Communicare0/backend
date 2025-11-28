package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.example.backend.entity.Comment;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private UUID commentId;
    private UUID postId;
    private String content;
    private UUID authorId;

    public static CommentResponse fromEntity(Comment comment) {
        return new CommentResponse(
            comment.getCommentId(),
            comment.getPost().getPostId(),
            comment.getContent(),
            comment.getAuthor().getUserId()
        );
    }
}