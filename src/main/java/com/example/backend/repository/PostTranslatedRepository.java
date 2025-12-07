package com.example.backend.repository;

import com.example.backend.entity.PostTranslated;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostTranslatedRepository extends JpaRepository<PostTranslated, UUID> {
    PostTranslated findByPost_PostId(UUID postPostId);
}
