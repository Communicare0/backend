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
    public Comment getCommentById(UUID id) {
        return commentRepository.findById(id).orElse(null);
    }

    @Override
    public List<Comment> findCommentsByPostId(UUID postId) {
        return commentRepository.findByPost_PostIdOrderByCreatedAtAsc(postId);
    }

    @Override
    @Transactional
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
    @Transactional
    public Comment updateComment(UUID commentId, UpdateCommentRequest updateCommentRequest) {
        Optional<Comment> optionalComment = commentRepository.findById(commentId);

        if (optionalComment.isPresent()) {
            Comment comment = optionalComment.get();

            if (updateCommentRequest.getContent() != null) {
                comment.setContent(updateCommentRequest.getContent());
            }

            return commentRepository.save(comment);
        } else {
            return null;
        }
    }

    @Override
    @Transactional
    public void deleteComment(UUID id) {
        commentRepository.deleteById(id);
    }
}