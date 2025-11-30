package com.example.backend.service;
import com.example.backend.dto.request.CreatePostRequest;
import com.example.backend.dto.request.UpdatePostRequest;
import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import com.example.backend.entity.enums.PostCategory;
import com.example.backend.entity.enums.PostStatus;
import com.example.backend.repository.PostRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
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
        //return postRepository.findById(id).orElse(null);
        return postRepository.findByPostIdAndStatusAndDeletedAtIsNull(id, PostStatus.VISIBLE);
    }

    @Override
    public List<Post> findPostsByUserId(UUID userId) {
        //return postRepository.findByAuthor_UserId(userId);
        return postRepository.findByAuthor_UserIdAndStatusAndDeletedAtIsNull(userId, PostStatus.VISIBLE);
    }

    @Override
    public List<Post> findPostsByCategory(PostCategory category) {
        //return postRepository.findByCategory(category);
        return postRepository.findByCategoryAndStatusAndDeletedAtIsNull(category, PostStatus.VISIBLE);
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

        post.setStatus(PostStatus.VISIBLE);

        post.setLikeCount(0);
        post.setViewCount(0);
        post.setTranslated(false);

        OffsetDateTime now = OffsetDateTime.now();
        post.setCreatedAt(now);
        post.setUpdatedAt(now);

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

        post.setUpdatedAt(OffsetDateTime.now());

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

        post.setStatus(PostStatus.DELETED);
        post.setDeletedAt(OffsetDateTime.now());
        post.setUpdatedAt(OffsetDateTime.now());

        postRepository.save(post);

    }
}
