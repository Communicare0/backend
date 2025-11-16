package com.example.backend.controller;

import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import com.example.backend.entity.enums.PostCategory;
import com.example.backend.entity.enums.PostStatus;
import com.example.backend.entity.enums.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Post Controller Integration Tests")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Post testPost;
    private UUID testPostId;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        testUser = userRepository.save(testUser);
        testPost = createTestPost(testUser);
        testPost = postRepository.save(testPost);
        testPostId = testPost.getPostId();
    }

    @Test
    @WithMockUser(username = "1", roles = "USER")
    @DisplayName("게시글 생성 - 성공")
    void createPost_Success() throws Exception {
        PostCreateDto createDto = new PostCreateDto();
        createDto.setTitle("새로운 게시글");
        createDto.setContent("새로운 내용");
        createDto.setCategory(PostCategory.DAILY);

        mockMvc.perform(post("/api/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("새로운 게시글"))
                .andExpect(jsonPath("$.content").value("새로운 내용"))
                .andExpect(jsonPath("$.category").value("DAILY"))
                .andExpect(jsonPath("$.authorId").value(testUser.getUserId().toString()))
                .andExpect(jsonPath("$.likeCount").value(0))
                .andExpect(jsonPath("$.viewCount").value(0));
    }

    @Test
    @WithMockUser(username = "1", roles = "USER")
    @DisplayName("게시글 생성 - validation 실패")
    void createPost_ValidationFail() throws Exception {
        PostCreateDto createDto = new PostCreateDto();
        createDto.setTitle("");
        createDto.setContent("");
        createDto.setCategory(null);

        mockMvc.perform(post("/api/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("전체 게시글 목록 조회 - 성공")
    void getAllPosts_Success() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDir", "desc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].postId").value(testPostId.toString()))
                .andExpect(jsonPath("$.content[0].title").value(testPost.getTitle()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("특정 게시글 조회 - 성공")
    void getPostById_Success() throws Exception {
        mockMvc.perform(get("/api/posts/{postId}", testPostId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(testPostId.toString()))
                .andExpect(jsonPath("$.title").value(testPost.getTitle()))
                .andExpect(jsonPath("$.content").value(testPost.getContent()))
                .andExpect(jsonPath("$.viewCount").value(1));
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 - 실패")
    void getPostById_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/api/posts/{postId}", nonExistentId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "1", roles = "USER")
    @DisplayName("게시글 수정 - 성공")
    void updatePost_Success() throws Exception {
        PostUpdateDto updateDto = new PostUpdateDto();
        updateDto.setTitle("수정된 제목");
        updateDto.setContent("수정된 내용");
        updateDto.setCategory(PostCategory.QUESTION);

        mockMvc.perform(put("/api/posts/{postId}", testPostId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 제목"))
                .andExpect(jsonPath("$.content").value("수정된 내용"))
                .andExpect(jsonPath("$.category").value("QUESTION"));
    }

    @Test
    @WithMockUser(username = "999", roles = "USER")
    @DisplayName("타인 게시글 수정 - 실패")
    void updatePost_Unauthorized() throws Exception {
        PostUpdateDto updateDto = new PostUpdateDto();
        updateDto.setTitle("수정된 제목");

        mockMvc.perform(put("/api/posts/{postId}", testPostId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "1", roles = "USER")
    @DisplayName("게시글 삭제 - 성공")
    void deletePost_Success() throws Exception {
        mockMvc.perform(delete("/api/posts/{postId}", testPostId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/posts/{postId}", testPostId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "999", roles = "USER")
    @DisplayName("타인 게시글 삭제 - 실패")
    void deletePost_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/posts/{postId}", testPostId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("작성자별 게시글 조회 - 성공")
    void getPostsByAuthor_Success() throws Exception {
        mockMvc.perform(get("/api/posts/author/{authorId}", testUser.getUserId())
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].authorId").value(testUser.getUserId().toString()));
    }

    @Test
    @DisplayName("게시글 검색 - 성공")
    void searchPosts_Success() throws Exception {
        mockMvc.perform(get("/api/posts/search")
                        .param("keyword", "테스트")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value(testPost.getTitle()));
    }

    @Test
    @DisplayName("좋아요 증가 - 성공")
    void likePost_Success() throws Exception {
        mockMvc.perform(post("/api/posts/{postId}/like", testPostId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/{postId}", testPostId))
                .andDo(print())
                .andExpect(jsonPath("$.likeCount").value(1));
    }

    @Test
    @DisplayName("좋아요 감소 - 성공")
    void unlikePost_Success() throws Exception {
        testPost.setLikeCount(1);
        postRepository.save(testPost);

        mockMvc.perform(delete("/api/posts/{postId}/like", testPostId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/{postId}", testPostId))
                .andDo(print())
                .andExpect(jsonPath("$.likeCount").value(0));
    }

    @Test
    @DisplayName("존재하지 않는 게시글에 좋아요 시도 - 실패")
    void likePost_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(post("/api/posts/{postId}/like", nonExistentId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("페이징 파라미터 테스트 - 다른 페이지")
    void getAllPosts_DifferentPage() throws Exception {
        for (int i = 0; i < 15; i++) {
            Post post = createTestPost(testUser);
            post.setTitle("게시글 " + i);
            postRepository.save(post);
        }

        mockMvc.perform(get("/api/posts")
                        .param("page", "1")
                        .param("size", "10")
                        .param("sortBy", "title")
                        .param("sortDir", "asc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.number").value(1));
    }

    private User createTestUser() {
        return User.builder()
                .userId(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("testUser")
                .friendCode("TEST123456")
                .role(UserRole.USER)
                .status(com.example.backend.entity.enums.AccountStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    private Post createTestPost(User author) {
        return Post.builder()
                .postId(UUID.randomUUID())
                .author(author)
                .title("테스트 게시글")
                .content("테스트 내용")
                .category(PostCategory.STUDY)
                .isTranslated(false)
                .status(PostStatus.VISIBLE)
                .viewCount(0)
                .likeCount(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }
}
