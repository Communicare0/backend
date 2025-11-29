package com.example.backend.service;
import com.example.backend.dto.request.CreatePostRequest;
import com.example.backend.dto.request.UpdatePostRequest;
import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import com.example.backend.repository.PostRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    public Post createPost(UUID userId, CreatePostRequest createPostRequest) {
        User user = userRepository.getReferenceById(userId);
        Post post = new Post();
        post.setAuthor(user);
        post.setTitle(createPostRequest.getTitle());
        post.setContent(createPostRequest.getContent());
        post.setCategory(createPostRequest.getCategory());

        return postRepository.save(post);
    }

    @Override
    @Transactional
    public Post updatePost(UUID userId, UUID postId, UpdatePostRequest updatePostRequest) {
        Optional<Post> postOpt = postRepository.findById(postId);

        if (postOpt.isEmpty()) {
            return null;
        }

        Post post = postOpt.get();

        // 작성자와 로그인 유저 일치 여부 확인
        if (!post.getAuthor().getUserId().equals(userId)) {

            return null;
        }

        if (updatePostRequest.getTitle() != null) {
            post.setTitle(updatePostRequest.getTitle());
        }
        if (updatePostRequest.getContent() != null) {
            post.setContent(updatePostRequest.getContent());
        }

        return postRepository.save(post);
    }

    @Override
    @Transactional
    public void deletePost(UUID userId, UUID postId) {
        Optional<Post> postOpt = postRepository.findById(postId);

        if (postOpt.isEmpty()) {
            return;
        }

        Post post = postOpt.get();

        // 작성자와 로그인 유저 일치 여부 확인
        if (!post.getAuthor().getUserId().equals(userId)) {
            return;
        }

        postRepository.deleteById(postId);
    }
}
