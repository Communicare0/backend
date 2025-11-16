package com.example.backend.repository;

import com.example.backend.entity.User;
import com.example.backend.entity.enums.AccountStatus;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("User Repository Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        entityManager.persist(testUser);
        entityManager.flush();
    }

    @Test
    @DisplayName("ID로 삭제되지 않은 사용자 조회")
    void findByUserIdAndDeletedAtIsNull() {
        var found = userRepository.findByUserIdAndDeletedAtIsNull(testUser.getUserId());

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(testUser.getUserId());
        assertThat(found.get().getEmail()).isEqualTo(testUser.getEmail());
    }

    @Test
    @DisplayName("삭제된 사용자는 조회되지 않음")
    void deletedUserNotFound() {
        testUser.setDeletedAt(OffsetDateTime.now());
        entityManager.merge(testUser);
        entityManager.flush();

        var found = userRepository.findByUserIdAndDeletedAtIsNull(testUser.getUserId());

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("이메일로 사용자 조회")
    void findByEmailAndDeletedAtIsNull() {
        var found = userRepository.findByEmailAndDeletedAtIsNull(testUser.getEmail());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(testUser.getEmail());
    }

    @Test
    @DisplayName("닉네임으로 사용자 조회")
    void findByNicknameAndDeletedAtIsNull() {
        var found = userRepository.findByNicknameAndDeletedAtIsNull(testUser.getNickname());

        assertThat(found).isPresent();
        assertThat(found.get().getNickname()).isEqualTo(testUser.getNickname());
    }

    @Test
    @DisplayName("친구 코드로 사용자 조회")
    void findByFriendCodeAndDeletedAtIsNull() {
        var found = userRepository.findByFriendCodeAndDeletedAtIsNull(testUser.getFriendCode());

        assertThat(found).isPresent();
        assertThat(found.get().getFriendCode()).isEqualTo(testUser.getFriendCode());
    }

    @Test
    @DisplayName("이메일 중복 확인 - 존재함")
    void existsByEmailAndDeletedAtIsNull_Exists() {
        boolean exists = userRepository.existsByEmailAndDeletedAtIsNull(testUser.getEmail());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("이메일 중복 확인 - 존재하지 않음")
    void existsByEmailAndDeletedAtIsNull_NotExists() {
        boolean exists = userRepository.existsByEmailAndDeletedAtIsNull("nonexistent@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("닉네임 중복 확인")
    void existsByNicknameAndDeletedAtIsNull() {
        boolean exists = userRepository.existsByNicknameAndDeletedAtIsNull(testUser.getNickname());
        boolean notExists = userRepository.existsByNicknameAndDeletedAtIsNull("nonexistent");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("친구 코드 중복 확인")
    void existsByFriendCodeAndDeletedAtIsNull() {
        boolean exists = userRepository.existsByFriendCodeAndDeletedAtIsNull(testUser.getFriendCode());
        boolean notExists = userRepository.existsByFriendCodeAndDeletedAtIsNull("NONEXISTENT");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("ID 존재 확인")
    void existsByUserIdAndDeletedAtIsNull() {
        boolean exists = userRepository.existsByUserIdAndDeletedAtIsNull(testUser.getUserId());
        boolean notExists = userRepository.existsByUserIdAndDeletedAtIsNull(999L);

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("삭제되지 않은 사용자 목록 페이징 조회")
    void findByDeletedAtIsNull() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> users = userRepository.findByDeletedAtIsNull(pageable);

        assertThat(users).hasSize(1);
        assertThat(users.getContent().get(0).getUserId()).isEqualTo(testUser.getUserId());
    }

    @Test
    @DisplayName("권한별 사용자 조회")
    void findByRoleAndDeletedAtIsNull() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userRoleUsers = userRepository.findByRoleAndDeletedAtIsNull(UserRole.USER, pageable);
        Page<User> adminRoleUsers = userRepository.findByRoleAndDeletedAtIsNull(UserRole.ADMIN, pageable);

        assertThat(userRoleUsers).hasSize(1);
        assertThat(adminRoleUsers).isEmpty();
    }

    @Test
    @DisplayName("상태별 사용자 조회")
    void findByStatusAndDeletedAtIsNull() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> activeUsers = userRepository.findByStatusAndDeletedAtIsNull(AccountStatus.ACTIVE, pageable);
        Page<User> inactiveUsers = userRepository.findByStatusAndDeletedAtIsNull(AccountStatus.INACTIVE, pageable);

        assertThat(activeUsers).hasSize(1);
        assertThat(inactiveUsers).isEmpty();
    }

    @Test
    @DisplayName("부서별 사용자 조회")
    void findByDepartmentAndDeletedAtIsNull() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> csUsers = userRepository.findByDepartmentAndDeletedAtIsNull("Computer Science", pageable);
        Page<User> mathUsers = userRepository.findByDepartmentAndDeletedAtIsNull("Mathematics", pageable);

        assertThat(csUsers).hasSize(1);
        assertThat(mathUsers).isEmpty();
    }

    @Test
    @DisplayName("키워드로 사용자 검색 - 이메일")
    void searchByKeyword_Email() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> foundUsers = userRepository.searchByKeyword("test@example", pageable);

        assertThat(foundUsers).hasSize(1);
        assertThat(foundUsers.getContent().get(0).getEmail()).contains("test@example");
    }

    @Test
    @DisplayName("키워드로 사용자 검색 - 닉네임")
    void searchByKeyword_Nickname() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> foundUsers = userRepository.searchByKeyword("testUser", pageable);

        assertThat(foundUsers).hasSize(1);
        assertThat(foundUsers.getContent().get(0).getNickname()).contains("testUser");
    }

    @Test
    @DisplayName("키워드로 사용자 검색 - 부서")
    void searchByKeyword_Department() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> foundUsers = userRepository.searchByKeyword("Computer", pageable);

        assertThat(foundUsers).hasSize(1);
        assertThat(foundUsers.getContent().get(0).getDepartment()).contains("Computer");
    }

    @Test
    @DisplayName("상태별 사용자 수 계산")
    void countByStatus() {
        long activeCount = userRepository.countByStatus(AccountStatus.ACTIVE);
        long inactiveCount = userRepository.countByStatus(AccountStatus.INACTIVE);

        assertThat(activeCount).isEqualTo(1);
        assertThat(inactiveCount).isEqualTo(0);
    }

    @Test
    @DisplayName("권한별 사용자 수 계산")
    void countByRole() {
        long userCount = userRepository.countByRole(UserRole.USER);
        long adminCount = userRepository.countByRole(UserRole.ADMIN);

        assertThat(userCount).isEqualTo(1);
        assertThat(adminCount).isEqualTo(0);
    }

    @Test
    @DisplayName("전체 활성 사용자 수 계산")
    void countActiveUsers() {
        long activeCount = userRepository.countActiveUsers();

        assertThat(activeCount).isEqualTo(1);
    }

    @Test
    @DisplayName("대소문자 구분 없는 검색")
    void searchByKeyword_CaseInsensitive() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> foundUsers = userRepository.searchByKeyword("TESTUSER", pageable);

        assertThat(foundUsers).hasSize(1);
        assertThat(foundUsers.getContent().get(0).getNickname()).isEqualTo("testUser");
    }

    private User createTestUser() {
        return User.builder()
                .userId(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("testUser")
                .department("Computer Science")
                .studentId("20230001")
                .nationality("Korea")
                .language("Korean")
                .profileImageUrl("http://example.com/profile.jpg")
                .friendCode("TEST123456")
                .role(UserRole.USER)
                .status(AccountStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }
}