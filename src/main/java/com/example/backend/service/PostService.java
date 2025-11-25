package com.example.backend.service;

import com.example.backend.entity.Post;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public interface PostService {
    Post getPostById(UUID id);

    List<Post> findPostsByUserId(UUID userId);

    List<Post> findPostsByCategory(String category);

    @Transactional
    Post createPost(UUID userId, Post post);

    @Transactional
    Post updatePost(Post post);

    @Transactional
    void deletePost(UUID id);
}
