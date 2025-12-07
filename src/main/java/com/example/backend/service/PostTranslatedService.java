package com.example.backend.service;

import com.example.backend.dto.request.CreatePostTranslatedRequest;
import com.example.backend.entity.PostTranslated;

import java.util.UUID;

public interface PostTranslatedService {
    PostTranslated getPostTranslatedById(UUID id);
    PostTranslated getPostTranslatedByPostId(UUID postId);
    PostTranslated createPostTranslated(CreatePostTranslatedRequest request);
}
