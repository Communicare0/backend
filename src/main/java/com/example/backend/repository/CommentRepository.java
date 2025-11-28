package com.example.backend.repository;

import com.example.backend.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID>, CommentRepositoryCustom {
    
    List<Comment> findByPost_PostIdOrderByCreatedAtAsc(UUID postId);
}
