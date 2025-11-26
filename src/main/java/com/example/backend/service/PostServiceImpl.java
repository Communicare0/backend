package com.example.backend.service;
import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import com.example.backend.repository.PostRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PostServiceImpl implements PostService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public PostServiceImpl(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    @Override
    public Post getPostById(UUID id) {
        return postRepository.findById(id).orElse(null);
    }

    @Override
    public List<Post> findPostsByUserId(UUID userId) {
        return postRepository.findByAuthor_UserId(userId);
    }

    @Override
    public List<Post> findPostsByCategory(String category) {
        return List.of();
    }

    @Override
    @Transactional
    public Post createPost(UUID userId, Post post) {
        User user = userRepository.getReferenceById(userId);
        post.setAuthor(user);

        return postRepository.save(post);
    }

    @Override
    @Transactional
    public Post updatePost(Post post) {
        return postRepository.save(post);
    }

    @Override
    @Transactional
    public void deletePost(UUID id) {
        postRepository.deleteById(id);
    }
}
