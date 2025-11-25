package com.example.backend.controller;

import com.example.backend.dto.request.CreatePostRequest;
import com.example.backend.dto.request.UpdatePostRequest;
import com.example.backend.dto.response.PostResponse;
import com.example.backend.entity.Post;
import com.example.backend.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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

    // TODO(hj): remove this placeholder user ID and use actual authenticated user ID.
    private final UUID PLACEHOLDER_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    public PostController(PostService postService) {
        this.postService = postService;
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
        // TODO(hj): Get authenticated user ID from security context
        UUID userId = PLACEHOLDER_USER_ID;

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
        UUID userId = PLACEHOLDER_USER_ID;

        if (id == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Post post = postService.getPostById(id);
        PostResponse postResponse = PostResponse.fromEntity(post);
        return ResponseEntity.ok(postResponse);
    }

    @PostMapping
    @Operation(
        summary = "Create a new post",
        description = "Create a new post",
        requestBody = @RequestBody(
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
    public ResponseEntity<PostResponse> createPost(@RequestBody CreatePostRequest createPostRequest) {
        UUID userId = PLACEHOLDER_USER_ID;

        try {
            if (createPostRequest.getTitle() == null || createPostRequest.getContent() == null || createPostRequest.getCategory() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            Post post = new Post();
            post.setTitle(createPostRequest.getTitle());
            post.setContent(createPostRequest.getContent());
            post.setCategory(createPostRequest.getCategory());

            Post createdPost = postService.createPost(userId, post);
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
        requestBody = @RequestBody(
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
        UUID userId = PLACEHOLDER_USER_ID;

        try {
            if (id == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            Post post = postService.getPostById(id);

            if (updatePostRequest.getTitle() != null)
                post.setTitle(updatePostRequest.getTitle());
            if (updatePostRequest.getContent() != null)
                post.setContent(updatePostRequest.getContent());

            Post updatedPost = postService.updatePost(post);
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
        UUID userId = PLACEHOLDER_USER_ID;

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

            postService.deletePost(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
