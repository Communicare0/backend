package com.example.backend.service;

import com.example.backend.entity.User;
import com.example.backend.entity.enums.AccountStatus;
import com.example.backend.entity.enums.UserRole;
import com.example.backend.exception.DuplicateResourceException;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserCreateDto createDto;
    private UserUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        createDto = createTestCreateDto();
        updateDto = createTestUpdateDto();
    }

    @Test
    @DisplayName("사용자 생성 성공")
    void createUser() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.existsByEmailAndDeletedAtIsNull(anyString())).thenReturn(false);
        when(userRepository.existsByNicknameAndDeletedAtIsNull(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponseDto result = userService.createUser(createDto);

        assertThat(result.getEmail()).isEqualTo(createDto.getEmail());
        assertThat(result.getNickname()).isEqualTo(createDto.getNickname());
        verify(passwordEncoder).encode(createDto.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("이메일 중복으로 사용자 생성 실패")
    void createUser_DuplicateEmail() {
        when(userRepository.existsByEmailAndDeletedAtIsNull(anyString())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(createDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("닉네임 중복으로 사용자 생성 실패")
    void createUser_DuplicateNickname() {
        when(userRepository.existsByEmailAndDeletedAtIsNull(anyString())).thenReturn(false);
        when(userRepository.existsByNicknameAndDeletedAtIsNull(anyString())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(createDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Nickname already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("전체 사용자 목록 조회")
    void getAllUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userRepository.findByDeletedAtIsNull(pageable)).thenReturn(userPage);

        Page<UserResponseDto> result = userService.getAllUsers(pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo(testUser.getEmail());
        verify(userRepository).findByDeletedAtIsNull(pageable);
    }

    @Test
    @DisplayName("ID로 사용자 조회 성공")
    void getUserById() {
        when(userRepository.findByUserIdAndDeletedAtIsNull(testUser.getUserId()))
                .thenReturn(Optional.of(testUser));

        UserResponseDto result = userService.getUserById(testUser.getUserId());

        assertThat(result.getUserId()).isEqualTo(testUser.getUserId());
        verify(userRepository).findByUserIdAndDeletedAtIsNull(testUser.getUserId());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 실패")
    void getUserByIdNotFound() {
        when(userRepository.findByUserIdAndDeletedAtIsNull(testUser.getUserId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(testUser.getUserId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("이메일로 사용자 조회")
    void getUserByEmail() {
        when(userRepository.findByEmailAndDeletedAtIsNull(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));

        UserResponseDto result = userService.getUserByEmail(testUser.getEmail());

        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
        verify(userRepository).findByEmailAndDeletedAtIsNull(testUser.getEmail());
    }

    @Test
    @DisplayName("친구 코드로 사용자 조회")
    void getUserByFriendCode() {
        when(userRepository.findByFriendCodeAndDeletedAtIsNull(testUser.getFriendCode()))
                .thenReturn(Optional.of(testUser));

        UserResponseDto result = userService.getUserByFriendCode(testUser.getFriendCode());

        assertThat(result.getFriendCode()).isEqualTo(testUser.getFriendCode());
        verify(userRepository).findByFriendCodeAndDeletedAtIsNull(testUser.getFriendCode());
    }

    @Test
    @DisplayName("사용자 프로필 조회")
    void getUserProfile() {
        when(userRepository.findByUserIdAndDeletedAtIsNull(testUser.getUserId()))
                .thenReturn(Optional.of(testUser));

        UserProfileDto result = userService.getUserProfile(testUser.getUserId());

        assertThat(result.getUserId()).isEqualTo(testUser.getUserId());
        assertThat(result.getNickname()).isEqualTo(testUser.getNickname());
        assertThat(result.getDepartment()).isEqualTo(testUser.getDepartment());
        assertThat(result.getEmail()).isNull();
        verify(userRepository).findByUserIdAndDeletedAtIsNull(testUser.getUserId());
    }

    @Test
    @DisplayName("사용자 정보 수정 성공")
    void updateUser() {
        when(userRepository.findByUserIdAndDeletedAtIsNull(testUser.getUserId()))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponseDto result = userService.updateUser(testUser.getUserId(), updateDto);

        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("사용자 정보 수정 - 이메일 중복 실패")
    void updateUser_DuplicateEmail() {
        when(userRepository.findByUserIdAndDeletedAtIsNull(testUser.getUserId()))
                .thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndDeletedAtIsNull("new@example.com")).thenReturn(true);

        updateDto.setEmail("new@example.com");

        assertThatThrownBy(() -> userService.updateUser(testUser.getUserId(), updateDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 정보 수정 - 비밀번호 변경")
    void updateUser_PasswordChange() {
        when(userRepository.findByUserIdAndDeletedAtIsNull(testUser.getUserId()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword123!")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        updateDto.setPassword("newPassword123!");

        userService.updateUser(testUser.getUserId(), updateDto);

        verify(passwordEncoder).encode("newPassword123!");
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("사용자 삭제 성공")
    void deleteUser() {
        when(userRepository.findByUserIdAndDeletedAtIsNull(testUser.getUserId()))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.deleteUser(testUser.getUserId());

        assertThat(testUser.getDeletedAt()).isNotNull();
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("키워드로 사용자 검색")
    void searchUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userRepository.searchByKeyword("test", pageable)).thenReturn(userPage);

        Page<UserResponseDto> result = userService.searchUsers("test", pageable);

        assertThat(result).hasSize(1);
        verify(userRepository).searchByKeyword("test", pageable);
    }

    @Test
    @DisplayName("권한별 사용자 조회")
    void getUsersByRole() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userRepository.findByRoleAndDeletedAtIsNull(UserRole.USER, pageable)).thenReturn(userPage);

        Page<UserResponseDto> result = userService.getUsersByRole(UserRole.USER, pageable);

        assertThat(result).hasSize(1);
        verify(userRepository).findByRoleAndDeletedAtIsNull(UserRole.USER, pageable);
    }

    @Test
    @DisplayName("상태별 사용자 조회")
    void getUsersByStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userRepository.findByStatusAndDeletedAtIsNull(AccountStatus.ACTIVE, pageable)).thenReturn(userPage);

        Page<UserResponseDto> result = userService.getUsersByStatus(AccountStatus.ACTIVE, pageable);

        assertThat(result).hasSize(1);
        verify(userRepository).findByStatusAndDeletedAtIsNull(AccountStatus.ACTIVE, pageable);
    }

    @Test
    @DisplayName("부서별 사용자 조회")
    void getUsersByDepartment() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userRepository.findByDepartmentAndDeletedAtIsNull("Computer Science", pageable)).thenReturn(userPage);

        Page<UserResponseDto> result = userService.getUsersByDepartment("Computer Science", pageable);

        assertThat(result).hasSize(1);
        verify(userRepository).findByDepartmentAndDeletedAtIsNull("Computer Science", pageable);
    }

    @Test
    @DisplayName("친구 코드 재생성")
    void regenerateFriendCode() {
        when(userRepository.findByUserIdAndDeletedAtIsNull(testUser.getUserId()))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        String result = userService.regenerateFriendCode(testUser.getUserId());

        assertThat(result).hasSize(10);
        assertThat(result).matches("[A-Za-z0-9]{10}");
        verify(userRepository).save(testUser);
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

    private UserCreateDto createTestCreateDto() {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail("new@example.com");
        dto.setPassword("password123!");
        dto.setNickname("newUser");
        dto.setDepartment("Mathematics");
        dto.setStudentId("20230002");
        dto.setNationality("USA");
        dto.setLanguage("English");
        dto.setProfileImageUrl("http://example.com/new-profile.jpg");
        return dto;
    }

    private UserUpdateDto createTestUpdateDto() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setEmail("updated@example.com");
        dto.setPassword("newPassword123!");
        dto.setNickname("updatedUser");
        dto.setDepartment("Physics");
        dto.setStudentId("20230003");
        dto.setNationality("Japan");
        dto.setLanguage("Japanese");
        dto.setProfileImageUrl("http://example.com/updated-profile.jpg");
        dto.setFriendCode("NEW123456");
        dto.setRole(UserRole.ADMIN);
        dto.setStatus(AccountStatus.INACTIVE);
        return dto;
    }
}
