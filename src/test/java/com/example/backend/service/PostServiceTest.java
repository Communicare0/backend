package com.example.backend.service;

import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import com.example.backend.entity.enums.PostCategory;
import com.example.backend.entity.enums.PostStatus;
import com.example.backend.entity.enums.UserRole;
import com.example.backend.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Post Service Tests")
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostService postService;

    private User testUser;
    private Post testPost;
    private PostCreateDto createDto;
    private PostUpdateDto updateDto;
    private UUID testPostId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = createTestUser();
        testPostId = UUID.randomUUID();
        testPost = createTestPost(testPostId, testUser);
        createDto = createTestCreateDto();
        updateDto = createTestUpdateDto();
    }

    @Test
    @DisplayName("게시글 생성 성공")
    void createPost() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        PostResponseDto result = postService.createPost(createDto, testUserId);

        assertThat(result.getTitle()).isEqualTo(createDto.getTitle());
        assertThat(result.getContent()).isEqualTo(createDto.getContent());
        assertThat(result.getCategory()).isEqualTo(createDto.getCategory());
        verify(userRepository).findById(testUserId);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 게시글 생성 실패")
    void createPostWithNonExistentUser() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.createPost(createDto, testUserId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("전체 게시글 목록 조회")
    void getAllPosts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> postPage = new PageImpl<>(List.of(testPost));

        when(postRepository.findByDeletedAtIsNull(pageable)).thenReturn(postPage);

        Page<PostResponseDto> result = postService.getAllPosts(pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo(testPost.getTitle());
        verify(postRepository).findByDeletedAtIsNull(pageable);
    }

    @Test
    @DisplayName("ID로 게시글 조회 성공")
    void getPostById() {
        when(postRepository.findByPostIdAndDeletedAtIsNull(testPostId)).thenReturn(Optional.of(testPost));
        when(postRepository.save(testPost)).thenReturn(testPost);

        PostResponseDto result = postService.getPostById(testPostId);

        assertThat(result.getTitle()).isEqualTo(testPost.getTitle());
        assertThat(result.getViewCount()).isEqualTo(1);
        verify(postRepository).findByPostIdAndDeletedAtIsNull(testPostId);
        verify(postRepository).save(testPost);
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 실패")
    void getPostByIdNotFound() {
        when(postRepository.findByPostIdAndDeletedAtIsNull(testPostId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPostById(testPostId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Post not found");
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updatePost() {
        when(postRepository.findByPostIdAndDeletedAtIsNull(testPostId)).thenReturn(Optional.of(testPost));
        when(postRepository.save(testPost)).thenReturn(testPost);

        PostResponseDto result = postService.updatePost(testPostId, updateDto, testUser.getUserId());

        assertThat(result.getTitle()).isEqualTo(updateDto.getTitle());
        verify(postRepository).save(testPost);
    }

    @Test
    @DisplayName("타인 게시글 수정 실패")
    void updatePostByOtherUser() {
        UUID otherUserId = UUID.randomUUID();
        when(postRepository.findByPostIdAndDeletedAtIsNull(testPostId)).thenReturn(Optional.of(testPost));

        assertThatThrownBy(() -> postService.updatePost(testPostId, updateDto, otherUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not authorized");

        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deletePost() {
        when(postRepository.findByPostIdAndDeletedAtIsNull(testPostId)).thenReturn(Optional.of(testPost));
        when(postRepository.save(testPost)).thenReturn(testPost);

        postService.deletePost(testPostId, testUser.getUserId());

        assertThat(testPost.getDeletedAt()).isNotNull();
        verify(postRepository).save(testPost);
    }

    @Test
    @DisplayName("타인 게시글 삭제 실패")
    void deletePostByOtherUser() {
        UUID otherUserId = UUID.randomUUID();
        when(postRepository.findByPostIdAndDeletedAtIsNull(testPostId)).thenReturn(Optional.of(testPost));

        assertThatThrownBy(() -> postService.deletePost(testPostId, otherUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not authorized");

        assertThat(testPost.getDeletedAt()).isNull();
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("작성자별 게시글 조회")
    void getPostsByAuthor() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> postPage = new PageImpl<>(List.of(testPost));

        when(userRepository.existsById(testUser.getUserId())).thenReturn(true);
        when(postRepository.findByAuthorUserIdAndDeletedAtIsNull(testUser.getUserId(), pageable))
                .thenReturn(postPage);

        Page<PostResponseDto> result = postService.getPostsByAuthor(testUser.getUserId(), pageable);

        assertThat(result).hasSize(1);
        verify(postRepository).findByAuthorUserIdAndDeletedAtIsNull(testUser.getUserId(), pageable);
    }

    @Test
    @DisplayName("키워드로 게시글 검색")
    void searchPosts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> postPage = new PageImpl<>(List.of(testPost));

        when(postRepository.searchByKeyword("테스트", pageable)).thenReturn(postPage);

        Page<PostResponseDto> result = postService.searchPosts("테스트", pageable);

        assertThat(result).hasSize(1);
        verify(postRepository).searchByKeyword("테스트", pageable);
    }

    @Test
    @DisplayName("좋아요 수 증가")
    void incrementLikeCount() {
        when(postRepository.findByPostIdAndDeletedAtIsNull(testPostId)).thenReturn(Optional.of(testPost));
        when(postRepository.save(testPost)).thenReturn(testPost);

        postService.incrementLikeCount(testPostId);

        assertThat(testPost.getLikeCount()).isEqualTo(1);
        verify(postRepository).save(testPost);
    }

    @Test
    @DisplayName("좋아요 수 감소")
    void decrementLikeCount() {
        testPost.setLikeCount(1);
        when(postRepository.findByPostIdAndDeletedAtIsNull(testPostId)).thenReturn(Optional.of(testPost));
        when(postRepository.save(testPost)).thenReturn(testPost);

        postService.decrementLikeCount(testPostId);

        assertThat(testPost.getLikeCount()).isEqualTo(0);
        verify(postRepository).save(testPost);
    }

    @Test
    @DisplayName("좋아요 수 0일 때 감소 시도")
    void decrementLikeCountFromZero() {
        when(postRepository.findByPostIdAndDeletedAtIsNull(testPostId)).thenReturn(Optional.of(testPost));
        when(postRepository.save(testPost)).thenReturn(testPost);

        postService.decrementLikeCount(testPostId);

        assertThat(testPost.getLikeCount()).isEqualTo(0);
        verify(postRepository).save(testPost);
    }

    private User createTestUser() {
        return User.builder()
                .userId(testUserId)
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

    private Post createTestPost(UUID postId, User author) {
        return Post.builder()
                .postId(postId)
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

    private PostCreateDto createTestCreateDto() {
        PostCreateDto dto = new PostCreateDto();
        dto.setTitle("새 게시글");
        dto.setContent("새 내용");
        dto.setCategory(PostCategory.DAILY);
        return dto;
    }

    private PostUpdateDto createTestUpdateDto() {
        PostUpdateDto dto = new PostUpdateDto();
        dto.setTitle("수정된 게시글");
        dto.setContent("수정된 내용");
        dto.setCategory(PostCategory.QUESTION);
        return dto;
    }
}
