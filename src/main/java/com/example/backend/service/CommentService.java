package com.example.backend.service;

import com.example.backend.dto.request.CreateCommentRequest;
import com.example.backend.dto.request.UpdateCommentRequest;
import com.example.backend.entity.Comment;

import java.util.List;
import java.util.UUID;

public interface CommentService {

    Comment getCommentById(UUID id);

    List<Comment> findCommentsByPostId(UUID postId);

    Comment createComment(UUID userId, CreateCommentRequest createCommentRequest);

    Comment updateComment(UUID commentId, UpdateCommentRequest updateCommentRequest);

    void deleteComment(UUID id);
}