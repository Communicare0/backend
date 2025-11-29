package com.example.backend.service;

import com.example.backend.dto.request.CreateCommentRequest;
import com.example.backend.dto.request.UpdateCommentRequest;
import com.example.backend.entity.Comment;
import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import com.example.backend.repository.CommentRepository;
import com.example.backend.repository.PostRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public List<Comment> findCommentsByPostId(UUID postId) {
        return commentRepository.findByPost_PostIdOrderByCreatedAtAsc(postId);
    }

    @Override
    public Comment createComment(UUID userId, CreateCommentRequest createCommentRequest) {
        User user = userRepository.getReferenceById(userId);
        Post post = postRepository.getReferenceById(createCommentRequest.getPostId());

        Comment comment = new Comment();
        comment.setAuthor(user);
        comment.setPost(post);
        comment.setContent(createCommentRequest.getContent());

        return commentRepository.save(comment);
    }

    @Override
    public Comment updateComment(UUID userId, UUID commentId, UpdateCommentRequest updateCommentRequest) {
        Optional<Comment> optionalComment = commentRepository.findById(commentId);

        if (optionalComment.isEmpty()) {
            return null; 
        }

        Comment comment = optionalComment.get();

        // 로그인 유저와 작성자 일치 여부 확인
        if (!comment.getAuthor().getUserId().equals(userId)) {

            return null;
        }

        if (updateCommentRequest.getContent() != null) {
            comment.setContent(updateCommentRequest.getContent());
        }

        return commentRepository.save(comment);
    }

    @Override
    public void deleteComment(UUID userId, UUID id) {
        Optional<Comment> optionalComment = commentRepository.findById(id);

        if (optionalComment.isEmpty()) {
            return; // 없는 댓글이면 그냥 무시
        }

        Comment comment = optionalComment.get();

        // 로그인 유저와 작성자 일치 여부 확인
        if (!comment.getAuthor().getUserId().equals(userId)) {
            return;
        }

        commentRepository.deleteById(id);
    }
}