package com.example.backend.service;
import com.example.backend.dto.request.CreatePostRequest;
import com.example.backend.dto.request.CreatePostTranslatedRequest;
import com.example.backend.dto.request.UpdatePostRequest;
import com.example.backend.entity.Post;
import com.example.backend.entity.PostLike;
import com.example.backend.entity.PostTranslated;
import com.example.backend.entity.User;
import com.example.backend.entity.enums.PostCategory;
import com.example.backend.entity.enums.PostStatus;
import com.example.backend.repository.PostRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.PostLikeRepository;
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
    private final PostLikeRepository postLikeRepository;
    private final PostTranslatedService postTranslatedService;
    
    public PostServiceImpl(
        UserRepository userRepository,
        PostRepository postRepository,
        PostTranslatedService postTranslatedService,
        PostLikeRepository postLikeRepository
    ) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.postTranslatedService = postTranslatedService;
        this.postLikeRepository = postLikeRepository;
    }

    @Override
    public Post getPostById(UUID id) {
        //return postRepository.findById(id).orElse(null);
        Post post = postRepository.findByPostIdAndStatusAndDeletedAtIsNull(id, PostStatus.VISIBLE);

        if (post == null) {
            return null;
        }

        // 조회수 증가
        int currentView = post.getViewCount();
        post.setViewCount(currentView + 1);
        post.setUpdatedAt(OffsetDateTime.now());

        return postRepository.save(post);
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

    @Override
    @Transactional
    public Post likePost(UUID userId, UUID postId) {
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        var post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        // 이미 좋아요 눌렀으면 아무 것도 안 하고 그대로 리턴 (idempotent)
        boolean alreadyLiked =
            postLikeRepository.existsByUser_UserIdAndPost_PostIdAndDeletedAtIsNull(userId, postId);

        if (alreadyLiked) {
            return post;
        }

        // PostLike 생성
        var like = new PostLike();
        like.setPostLikeId(UUID.randomUUID());
        like.setUser(user);
        like.setPost(post);
        var now = OffsetDateTime.now();
        like.setCreatedAt(now);
        like.setUpdatedAt(now);
        postLikeRepository.save(like);

        // 게시글 likeCount 증가
        post.setLikeCount(post.getLikeCount() + 1);
        post.setUpdatedAt(now);

        return postRepository.save(post);
    }

    @Override
    @Transactional
    public Post unlikePost(UUID userId, UUID postId) {
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        var post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        // 이미 누른 좋아요가 있는지 확인
        Optional<PostLike> likeOpt =
            postLikeRepository.findByUser_UserIdAndPost_PostIdAndDeletedAtIsNull(userId, postId);

        // 아직 좋아요 안 눌렀으면 아무 것도 안 하고 게시글 그대로 반환 (idempotent)
        if (likeOpt.isEmpty()) {
            return post;
        }

        PostLike like = likeOpt.get();
        OffsetDateTime now = OffsetDateTime.now();

        // soft delete 방식
        like.setDeletedAt(now);
        like.setUpdatedAt(now);
        postLikeRepository.save(like);

        // likeCount 감소 (음수 방지)
        int currentLikeCount = post.getLikeCount();
        post.setLikeCount(Math.max(0, currentLikeCount - 1));
        post.setUpdatedAt(now);

        return postRepository.save(post);
    }
}
