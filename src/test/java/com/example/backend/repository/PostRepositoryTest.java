package com.example.backend.repository;

import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import com.example.backend.entity.enums.PostCategory;
import com.example.backend.entity.enums.PostStatus;
import com.example.backend.entity.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Post Repository Tests")
class PostRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PostRepository postRepository;

    private User testUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        testPost = createTestPost(testUser);

        entityManager.persist(testUser);
        entityManager.persist(testPost);
        entityManager.flush();
    }

    @Test
    @DisplayName("게시글 ID로 삭제되지 않은 게시글 조회")
    void findByPostIdAndDeletedAtIsNull() {
        Optional<Post> found = postRepository.findByPostIdAndDeletedAtIsNull(testPost.getPostId());

        assertThat(found).isPresent();
        assertThat(found.get().getPostId()).isEqualTo(testPost.getPostId());
        assertThat(found.get().getTitle()).isEqualTo(testPost.getTitle());
    }

    @Test
    @DisplayName("삭제된 게시글은 조회되지 않음")
    void deletedPostNotFound() {
        testPost.setDeletedAt(OffsetDateTime.now());
        entityManager.merge(testPost);
        entityManager.flush();

        Optional<Post> found = postRepository.findByPostIdAndDeletedAtIsNull(testPost.getPostId());

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("삭제되지 않은 게시글 목록 페이징 조회")
    void findByDeletedAtIsNull() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> posts = postRepository.findByDeletedAtIsNull(pageable);

        assertThat(posts).hasSize(1);
        assertThat(posts.getContent().get(0).getPostId()).isEqualTo(testPost.getPostId());
    }

    @Test
    @DisplayName("상태별 게시글 조회")
    void findByStatusAndDeletedAtIsNull() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> visiblePosts = postRepository.findByStatusAndDeletedAtIsNull(PostStatus.VISIBLE, pageable);
        Page<Post> hiddenPosts = postRepository.findByStatusAndDeletedAtIsNull(PostStatus.HIDDEN, pageable);

        assertThat(visiblePosts).hasSize(1);
        assertThat(hiddenPosts).isEmpty();
    }

    @Test
    @DisplayName("카테고리별 게시글 조회")
    void findByCategoryAndDeletedAtIsNull() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> studyPosts = postRepository.findByCategoryAndDeletedAtIsNull(PostCategory.STUDY, pageable);
        Page<Post> dailyPosts = postRepository.findByCategoryAndDeletedAtIsNull(PostCategory.DAILY, pageable);

        assertThat(studyPosts).hasSize(1);
        assertThat(dailyPosts).isEmpty();
    }

    @Test
    @DisplayName("작성자별 게시글 조회")
    void findByAuthorUserIdAndDeletedAtIsNull() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> userPosts = postRepository.findByAuthorUserIdAndDeletedAtIsNull(testUser.getUserId(), pageable);
        Page<Post> otherUserPosts = postRepository.findByAuthorUserIdAndDeletedAtIsNull(999L, pageable);

        assertThat(userPosts).hasSize(1);
        assertThat(otherUserPosts).isEmpty();
    }

    @Test
    @DisplayName("키워드로 게시글 검색 - 제목")
    void searchByKeywordTitle() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> foundPosts = postRepository.searchByKeyword("테스트", pageable);

        assertThat(foundPosts).hasSize(1);
        assertThat(foundPosts.getContent().get(0).getTitle()).contains("테스트");
    }

    @Test
    @DisplayName("키워드로 게시글 검색 - 내용")
    void searchByKeywordContent() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> foundPosts = postRepository.searchByKeyword("내용", pageable);

        assertThat(foundPosts).hasSize(1);
        assertThat(foundPosts.getContent().get(0).getContent()).contains("내용");
    }

    @Test
    @DisplayName("상태별 게시글 수 계산")
    void countByStatus() {
        long visibleCount = postRepository.countByStatus(PostStatus.VISIBLE);
        long hiddenCount = postRepository.countByStatus(PostStatus.HIDDEN);

        assertThat(visibleCount).isEqualTo(1);
        assertThat(hiddenCount).isEqualTo(0);
    }

    @Test
    @DisplayName("게시글 존재 여부 확인")
    void existsByPostIdAndDeletedAtIsNull() {
        boolean exists = postRepository.existsByPostIdAndDeletedAtIsNull(testPost.getPostId());
        boolean notExists = postRepository.existsByPostIdAndDeletedAtIsNull(UUID.randomUUID());

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
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