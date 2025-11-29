package com.example.backend.service;

import com.example.backend.dto.request.CreatePostRequest;
import com.example.backend.dto.request.UpdatePostRequest;
import com.example.backend.entity.Post;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface PostService {
    Post getPostById(UUID id);

    List<Post> findPostsByUserId(UUID userId);

    List<Post> findPostsByCategory(String category);

    Post createPost(UUID userId, CreatePostRequest createPostRequest);

    Post updatePost(UUID userId, UUID postId, UpdatePostRequest updatePostRequest);

    void deletePost(UUID userId, UUID postId);
}
