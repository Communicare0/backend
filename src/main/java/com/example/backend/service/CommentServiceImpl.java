package com.example.backend.service;

import com.example.backend.dto.request.CreateCommentRequest;
import com.example.backend.dto.request.UpdateCommentRequest;
import com.example.backend.entity.Comment;
import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import com.example.backend.entity.enums.CommentStatus;
import com.example.backend.repository.CommentRepository;
import com.example.backend.repository.PostRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public CommentServiceImpl(
        UserRepository userRepository,
        PostRepository postRepository,
        CommentRepository commentRepository
    ) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Comment getCommentById(UUID id) {
        return commentRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> findCommentsByPostId(UUID id) {
        return commentRepository.findByPost_PostIdAndStatusAndDeletedAtIsNullOrderByCreatedAtAsc(
            id,
            CommentStatus.VISIBLE
        );
    }

    @Override
    public Comment createComment(UUID userId, CreateCommentRequest createCommentRequest) {
        User user = userRepository.getReferenceById(userId);
        Post post = postRepository.getReferenceById(createCommentRequest.getPostId());

        Comment comment = new Comment();
        comment.setCommentId(UUID.randomUUID());
        comment.setAuthor(user);
        comment.setPost(post);
        comment.setContent(createCommentRequest.getContent());

        var now = OffsetDateTime.now();
        comment.setCreatedAt(now);
        comment.setUpdatedAt(now);

        comment.setTranslated(false);

        return commentRepository.save(comment);
    }

   @Override
    public Comment updateComment(UUID userId, UUID commentId, UpdateCommentRequest updateCommentRequest) {
        Optional<Comment> optionalComment = commentRepository.findById(commentId);

        if (optionalComment.isEmpty()) {
            return null; 
        }

        Comment comment = optionalComment.get();

        if (!comment.getAuthor().getUserId().equals(userId)) {
            return null; 
        }

        if (comment.getStatus() != CommentStatus.VISIBLE) {
            return null;
        }

        if (updateCommentRequest.getContent() != null) {
            comment.setContent(updateCommentRequest.getContent());
        }

        comment.setUpdatedAt(OffsetDateTime.now());

        return commentRepository.save(comment);
    } 

    @Override
    public void deleteComment(UUID userId, UUID id) {
        Optional<Comment> optionalComment = commentRepository.findById(id);

        if (optionalComment.isEmpty()) {
            return;
        }

        Comment comment = optionalComment.get();

        if (!comment.getAuthor().getUserId().equals(userId)) {
            return;
        }

        if (comment.getDeletedAt() != null || comment.getStatus() != CommentStatus.VISIBLE) {
            return;
        }

        // 소프트 삭제 처리
        comment.setStatus(CommentStatus.HIDDEN);
        OffsetDateTime now = OffsetDateTime.now();
        comment.setDeletedAt(now);
        comment.setUpdatedAt(now);

        commentRepository.save(comment);
    } 
}