package com.example.backend.controller;

import com.example.backend.dto.request.CreatePostTranslatedRequest;
import com.example.backend.dto.response.PostTranslatedResponse;
import com.example.backend.entity.PostTranslated;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.PostTranslatedService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.backend.dto.request.CreatePostRequest;
import com.example.backend.dto.request.UpdatePostRequest;
import com.example.backend.dto.response.PostResponse;
import com.example.backend.entity.Post;
import com.example.backend.entity.enums.PostCategory;
import com.example.backend.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
// import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/posts")
@Tag(name = "Post", description = "Post API")
public class PostController {
    private final PostService postService;
    private final UserRepository userRepository;
    private final PostTranslatedService postTranslatedService;

    public PostController(
        PostService postService,
        UserRepository userRepository,
        PostTranslatedService postTranslatedService
    ) {
        this.postService = postService;
        this.userRepository = userRepository;
        this.postTranslatedService = postTranslatedService;
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication != null ? authentication.getPrincipal() : null;

        if (principal instanceof UUID uuid) {
            return uuid;
        }

        throw new IllegalStateException("인증 정보에서 사용자 ID를 찾을 수 없습니다.");
    }

    @GetMapping("/user")
    @Operation(
            summary = "Get all posts for the user",
            description = "Retrieves a list of all posts for a user",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = PostResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "500"
                    )
            }
    )
    public ResponseEntity<List<PostResponse>> getPosts() {

        UUID userId = getCurrentUserId();

        List<Post> posts = postService.findPostsByUserId(userId);

        if (posts.isEmpty()) {
            List<PostResponse> postResponses = new ArrayList<>();
            return ResponseEntity.ok(postResponses);
        }

        if (!posts.getFirst().getAuthor().getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<PostResponse> postResponses = posts.stream()
                .map(PostResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(postResponses);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get a specific post",
        parameters = {
            @Parameter(
                name = "id",
                description = "UUID of the specific post",
                required = true
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(schema = @Schema(implementation = PostResponse.class))
            ),
            @ApiResponse(
                responseCode = "500"
            )
        }
    )
    public ResponseEntity<PostResponse> getPost(@PathVariable UUID id) {

        UUID userId = getCurrentUserId();

        if (id == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Post post = postService.getPostById(id);
        PostResponse postResponse = PostResponse.fromEntity(post);
        return ResponseEntity.ok(postResponse);
    }

    @GetMapping("/translate/{id}")
    @Operation(
        summary = "Get a translated post",
        parameters = {
            @Parameter(
                name = "id",
                description = "UUID of the specific post",
                required = true
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(schema = @Schema(implementation = PostTranslatedResponse.class))
            ),
            @ApiResponse(
                responseCode = "500"
            )
        }
    )
    public ResponseEntity<PostTranslatedResponse> getPostTranslated(@PathVariable UUID id) {
        UUID userId = getCurrentUserId();
        if (id == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        Post post = postService.getPostById(id);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        CreatePostTranslatedRequest createPostTranslatedRequest = new CreatePostTranslatedRequest();
        createPostTranslatedRequest.setPostId(post.getPostId());
        createPostTranslatedRequest.setTitle(post.getTitle());
        createPostTranslatedRequest.setContent(post.getContent());
        createPostTranslatedRequest.setLanguage(user.getLanguage());

        PostTranslated postTranslated = postTranslatedService.createPostTranslated(createPostTranslatedRequest);

        PostTranslatedResponse postTranslatedResponse = new PostTranslatedResponse();
        postTranslatedResponse.setTranslatedTitle(postTranslated.getTranslatedTitle());
        postTranslatedResponse.setTranslatedContent(postTranslated.getTranslatedContent());
        return ResponseEntity.ok(postTranslatedResponse);
    }

    @GetMapping("/category/{category}")
    @Operation(
        summary = "Get posts by category",
        description = "Retrieves a list of posts filtered by category",
        parameters = {
            @Parameter(
                name = "category",
                description = "Post category (예: GENERAL, QNA, NOTICE 등 enum 값)",
                required = true
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Posts found",
                content = @Content(schema = @Schema(implementation = PostResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid category value"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        }
    )
    public ResponseEntity<List<PostResponse>> getPostsByCategory(
            @PathVariable PostCategory category
    ) {
        List<Post> posts = postService.findPostsByCategory(category);

        List<PostResponse> responses = posts.stream()
                .map(PostResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @Operation(
        summary = "Create a new post",
        description = "Create a new post",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "title, content, category",
            required = true,
            content = @Content(schema = @Schema(implementation = CreatePostRequest.class))
        ),
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Post created successfully",
                content = @Content(schema = @Schema(implementation = PostResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid input or post already exists for given year & season"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        }
    )
    public ResponseEntity<PostResponse> createPost(@org.springframework.web.bind.annotation.RequestBody CreatePostRequest createPostRequest) {
        UUID userId = getCurrentUserId();

        try {

            System.out.println(">>> CreatePostRequest: title=" + createPostRequest.getTitle()
                + ", content=" + createPostRequest.getContent()
                + ", category=" + createPostRequest.getCategory());


            if (createPostRequest.getTitle() == null || createPostRequest.getContent() == null || createPostRequest.getCategory() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            Post createdPost = postService.createPost(userId, createPostRequest);
            PostResponse postResponse = PostResponse.fromEntity(createdPost);
            return ResponseEntity.status(HttpStatus.CREATED).body(postResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update the post",
        description = "Update title, content of the post",
        parameters = {
            @Parameter(
                name = "id",
                description = "UUID of the post",
                required = true
            )
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(schema = @Schema(implementation = UpdatePostRequest.class))
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Post updated successfully",
                content = @Content(schema = @Schema(implementation = PostResponse.class))
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        }
    )
    public ResponseEntity<PostResponse> updatePost(@PathVariable UUID id, @RequestBody UpdatePostRequest updatePostRequest) {

        UUID userId = getCurrentUserId();

        try {
            if (id == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            Post updatedPost = postService.updatePost(userId, id, updatePostRequest);

            if (updatedPost == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            PostResponse postResponse = PostResponse.fromEntity(updatedPost);
            return ResponseEntity.ok(postResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete the post",
        parameters = {
            @Parameter(
                name = "id",
                description = "UUID of the post",
                required = true
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "204",
                description = "Post deleted successfully"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "User does not have access to this post"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Post not found"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        }
    )
    public ResponseEntity<Void> deletePost(@PathVariable UUID id) {
        UUID userId = getCurrentUserId();

        try {
            if (id == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            Post post = postService.getPostById(id);

            if (post == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            if (!post.getAuthor().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            postService.deletePost(userId, id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/like")
    @Operation(
        summary = "Like a post",
        description = "현재 로그인한 사용자가 해당 게시글에 좋아요를 누릅니다.",
        parameters = {
            @Parameter(
                name = "id",
                description = "UUID of the post",
                required = true
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Post liked successfully",
                content = @Content(schema = @Schema(implementation = PostResponse.class))
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        }
    )
    public ResponseEntity<PostResponse> likePost(@PathVariable UUID id) {
        UUID userId = getCurrentUserId();

        try {
            Post likedPost = postService.likePost(userId, id);
            PostResponse response = PostResponse.fromEntity(likedPost);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}/like")
    @Operation(
        summary = "Unlike a post",
        description = "현재 로그인한 사용자가 해당 게시글에 누른 좋아요를 취소합니다.",
        parameters = {
            @Parameter(
                name = "id",
                description = "UUID of the post",
                required = true
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Post unliked successfully",
                content = @Content(schema = @Schema(implementation = PostResponse.class))
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        }
    )
    public ResponseEntity<PostResponse> unlikePost(@PathVariable UUID id) {
        UUID userId = getCurrentUserId();

        try {
            Post unlikedPost = postService.unlikePost(userId, id);
            PostResponse response = PostResponse.fromEntity(unlikedPost);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search posts by keyword",
        description = "검색 키워드가 제목이나 내용에 포함된 게시글을 검색합니다.",
        parameters = {
            @Parameter(
                name = "keyword",
                description = "검색할 키워드 (제목이나 내용에 포함된 문자열)",
                required = true
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "검색된 게시글 목록",
                content = @Content(schema = @Schema(implementation = PostResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "유효하지 않은 검색 키워드"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류"
            )
        }
    )
    public ResponseEntity<List<PostResponse>> searchPosts(
        @RequestParam("keyword") String keyword
    ) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            List<Post> posts = postService.searchPosts(keyword);

            List<PostResponse> responses = posts.stream()
                    .map(PostResponse::fromEntity)
                    .toList();

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search/category/{category}")
    @Operation(
        summary = "Search posts by keyword in specific category",
        description = "특정 카테고리에서 검색 키워드가 제목이나 내용에 포함된 게시글을 검색합니다.",
        parameters = {
            @Parameter(
                name = "category",
                description = "게시글 카테고리 (GENERAL, QNA, NOTICE 등)",
                required = true
            ),
            @Parameter(
                name = "keyword",
                description = "검색할 키워드 (제목이나 내용에 포함된 문자열)",
                required = true
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "검색된 게시글 목록",
                content = @Content(schema = @Schema(implementation = PostResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "유효하지 않은 파라미터"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류"
            )
        }
    )
    public ResponseEntity<List<PostResponse>> searchPostsByCategory(
        @PathVariable PostCategory category,
        @RequestParam("keyword") String keyword
    ) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            List<Post> posts = postService.searchPostsByCategory(keyword, category);

            List<PostResponse> responses = posts.stream()
                    .map(PostResponse::fromEntity)
                    .toList();

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
