package com.example.backend.repository;

import com.example.backend.entity.PostLike;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {

    boolean existsByUser_UserIdAndPost_PostIdAndDeletedAtIsNull(UUID userId, UUID postId);

    Optional<PostLike> findByUser_UserIdAndPost_PostIdAndDeletedAtIsNull(UUID userId, UUID postId);

    long countByPost_PostIdAndDeletedAtIsNull(UUID postId);
}