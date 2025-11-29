package com.example.backend.controller;

import com.example.backend.dto.request.CreatePostRequest;
import com.example.backend.dto.request.UpdatePostRequest;
import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import com.example.backend.entity.enums.PostCategory;
import com.example.backend.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = PostController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)   // 🔹 Security 필터 비활성화
@ActiveProfiles("test")
@DisplayName("PostController 테스트")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Post testPost;
    private UUID testPostId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        // 🔹 테스트에서 사용할 고정 userId / postId
        testUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        testPostId = UUID.randomUUID();

        // 🔹 SecurityContext에 principal = UUID 세팅
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUserId);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        // 🔹 엔티티 기본 세팅
        testUser = new User();
        testUser.setUserId(testUserId);

        testPost = new Post();
        testPost.setPostId(testPostId);
        testPost.setAuthor(testUser);
        testPost.setTitle("테스트 게시물");
        testPost.setContent("테스트 내용입니다.");
        testPost.setCategory(PostCategory.GENERAL);
        testPost.setCreatedAt(OffsetDateTime.now());
        testPost.setUpdatedAt(OffsetDateTime.now());
    }

    @Test
    @DisplayName("사용자의 모든 게시물 조회 - 성공")
    void getPosts_Success() throws Exception {
        List<Post> posts = List.of(testPost);

        // controller에서 getCurrentUserId() → testUserId 사용
        when(postService.findPostsByUserId(eq(testUserId))).thenReturn(posts);

        mockMvc.perform(get("/v1/posts/user"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].postId").value(testPostId.toString()))
                .andExpect(jsonPath("$[0].title").value("테스트 게시물"));
    }

    @Test
    @DisplayName("사용자의 게시물 조회 - 빈 목록")
    void getPosts_EmptyList() throws Exception {
        when(postService.findPostsByUserId(eq(testUserId))).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/v1/posts/user"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("특정 게시물 조회 - 성공")
    void getPost_Success() throws Exception {
        when(postService.getPostById(testPostId)).thenReturn(testPost);

        mockMvc.perform(get("/v1/posts/{id}", testPostId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.postId").value(testPostId.toString()))
                .andExpect(jsonPath("$.title").value("테스트 게시물"))
                .andExpect(jsonPath("$.content").value("테스트 내용입니다."))
                .andExpect(jsonPath("$.category").value("GENERAL"));
    }

    @Test
    @DisplayName("게시물 생성 - 필드 누락으로 실패")
    void createPost_Fail_MissingFields() throws Exception {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle(null);   // 제목 없음
        request.setContent("내용만 있음");
        request.setCategory(PostCategory.GENERAL);

        mockMvc.perform(post("/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("게시물 수정 - 성공")
    void updatePost_Success() throws Exception {
        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("수정된 제목");
        request.setContent("수정된 내용");

        Post updatedPost = new Post();
        updatedPost.setPostId(testPostId);
        updatedPost.setAuthor(testUser);
        updatedPost.setTitle("수정된 제목");
        updatedPost.setContent("수정된 내용");
        updatedPost.setCategory(PostCategory.GENERAL);

        when(postService.updatePost(eq(testUserId), eq(testPostId), any(UpdatePostRequest.class)))
                .thenReturn(updatedPost);

        mockMvc.perform(put("/v1/posts/{id}", testPostId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("수정된 제목"))
                .andExpect(jsonPath("$.content").value("수정된 내용"));
    }

    @Test
    @DisplayName("게시물 수정 - 제목만 수정")
    void updatePost_TitleOnly() throws Exception {
        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("제목만 수정");
        request.setContent(null);

        Post updatedPost = new Post();
        updatedPost.setPostId(testPostId);
        updatedPost.setAuthor(testUser);
        updatedPost.setTitle("제목만 수정");
        updatedPost.setContent(testPost.getContent());
        updatedPost.setCategory(PostCategory.GENERAL);

        when(postService.updatePost(eq(testUserId), eq(testPostId), any(UpdatePostRequest.class)))
                .thenReturn(updatedPost);

        mockMvc.perform(put("/v1/posts/{id}", testPostId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("제목만 수정"))
                .andExpect(jsonPath("$.content").value("테스트 내용입니다."));
    }

    @Test
    @DisplayName("게시물 수정 - 내용만 수정")
    void updatePost_ContentOnly() throws Exception {
        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle(null);
        request.setContent("내용만 수정");

        Post updatedPost = new Post();
        updatedPost.setPostId(testPostId);
        updatedPost.setAuthor(testUser);
        updatedPost.setTitle(testPost.getTitle());
        updatedPost.setContent("내용만 수정");
        updatedPost.setCategory(PostCategory.GENERAL);

        when(postService.updatePost(eq(testUserId), eq(testPostId), any(UpdatePostRequest.class)))
                .thenReturn(updatedPost);

        mockMvc.perform(put("/v1/posts/{id}", testPostId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("테스트 게시물"))
                .andExpect(jsonPath("$.content").value("내용만 수정"));
    }

    @Test
    @DisplayName("게시물 삭제 - 성공")
    void deletePost_Success() throws Exception {
        when(postService.getPostById(testPostId)).thenReturn(testPost);

        mockMvc.perform(delete("/v1/posts/{id}", testPostId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("게시물 삭제 - 존재하지 않는 게시물")
    void deletePost_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(postService.getPostById(nonExistentId))
                .thenThrow(new EntityNotFoundException());

        mockMvc.perform(delete("/v1/posts/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("잘못된 UUID 형식으로 게시물 조회")
    void getPost_InvalidUUID() throws Exception {
        mockMvc.perform(get("/v1/posts/{id}", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("잘못된 UUID 형식으로 게시물 수정")
    void updatePost_InvalidUUID() throws Exception {
        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("수정");

        mockMvc.perform(put("/v1/posts/{id}", "invalid-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("잘못된 UUID 형식으로 게시물 삭제")
    void deletePost_InvalidUUID() throws Exception {
        mockMvc.perform(delete("/v1/posts/{id}", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("JSON 형식이 잘못된 요청")
    void createPost_InvalidJson() throws Exception {
        String invalidJson = "{title: \"제목\", content: \"내용\"}"; // 따옴표 없는 잘못된 JSON

        mockMvc.perform(post("/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}