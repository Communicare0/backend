package com.example.backend.controller;

import com.example.backend.entity.User;
import com.example.backend.entity.enums.AccountStatus;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("User Controller Integration Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User testAdmin;

    @BeforeEach
    void setUp() {
        testUser = createTestUser(1L, "user@example.com", "testUser", UserRole.USER);
        testUser = userRepository.save(testUser);

        testAdmin = createTestUser(2L, "admin@example.com", "testAdmin", UserRole.ADMIN);
        testAdmin = userRepository.save(testAdmin);
    }

    @Test
    @DisplayName("사용자 생성 - 성공")
    void createUser_Success() throws Exception {
        UserCreateDto createDto = new UserCreateDto();
        createDto.setEmail("newuser@example.com");
        createDto.setPassword("password123!");
        createDto.setNickname("newUser");
        createDto.setDepartment("Computer Science");
        createDto.setStudentId("20240001");
        createDto.setNationality("Korea");
        createDto.setLanguage("Korean");

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.nickname").value("newUser"))
                .andExpect(jsonPath("$.department").value("Computer Science"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.friendCode").exists())
                .andExpect(jsonPath("$.friendCode", hasLength(10)))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("사용자 생성 - validation 실패")
    void createUser_ValidationFail() throws Exception {
        UserCreateDto createDto = new UserCreateDto();
        createDto.setEmail("invalid-email");
        createDto.setPassword("123");
        createDto.setNickname(""); // 빈 닉네임
        createDto.setDepartment("");

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("전체 사용자 목록 조회 - 관리자")
    void getAllUsers_Admin() throws Exception {
        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDir", "desc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("전체 사용자 목록 조회 - 일반 사용자 (권한 없음)")
    void getAllUsers_User_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ID로 사용자 조회 - 성공")
    void getUserById_Success() throws Exception {
        mockMvc.perform(get("/api/users/{userId}", testUser.getUserId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getUserId()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.nickname").value(testUser.getNickname()))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 - 실패")
    void getUserById_NotFound() throws Exception {
        mockMvc.perform(get("/api/users/{userId}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("이메일로 사용자 조회 - 관리자")
    void getUserByEmail_Admin() throws Exception {
        mockMvc.perform(get("/api/users/email/{email}", testUser.getEmail()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testUser.getEmail()));
    }

    @Test
    @DisplayName("친구 코드로 사용자 조회 - 성공")
    void getUserByFriendCode_Success() throws Exception {
        mockMvc.perform(get("/api/users/friend-code/{friendCode}", testUser.getFriendCode()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.friendCode").value(testUser.getFriendCode()));
    }

    @Test
    @DisplayName("사용자 프로필 조회 - 성공")
    void getUserProfile_Success() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/profile", testUser.getUserId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getUserId()))
                .andExpect(jsonPath("$.nickname").value(testUser.getNickname()))
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.role").doesNotExist());
    }

    @Test
    @WithMockUser(username = "1", roles = "USER")
    @DisplayName("사용자 정보 수정 - 본인")
    void updateUser_ByOwner() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setNickname("updatedNickname");
        updateDto.setDepartment("Updated Department");

        mockMvc.perform(put("/api/users/{userId}", testUser.getUserId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("updatedNickname"))
                .andExpect(jsonPath("$.department").value("Updated Department"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("사용자 정보 수정 - 관리자")
    void updateUser_ByAdmin() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setNickname("adminUpdatedNickname");
        updateDto.setRole(UserRole.ADMIN);

        mockMvc.perform(put("/api/users/{userId}", testUser.getUserId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("adminUpdatedNickname"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @WithMockUser(username = "999", roles = "USER")
    @DisplayName("사용자 정보 수정 - 타인 (권한 없음)")
    void updateUser_ByOtherUser_Unauthorized() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setNickname("hackedNickname");

        mockMvc.perform(put("/api/users/{userId}", testUser.getUserId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "1", roles = "USER")
    @DisplayName("내 정보 수정 - 성공")
    void updateCurrentUser_Success() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setNickname("myNewNickname");
        updateDto.setPassword("newPassword123!");

        mockMvc.perform(put("/api/users/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("myNewNickname"));
    }

    @Test
    @WithMockUser(username = "1", roles = "USER")
    @DisplayName("사용자 삭제 - 본인")
    void deleteUser_ByOwner() throws Exception {
        mockMvc.perform(delete("/api/users/{userId}", testUser.getUserId())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/{userId}", testUser.getUserId()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "999", roles = "USER")
    @DisplayName("사용자 삭제 - 타인 (권한 없음)")
    void deleteUser_ByOtherUser_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/users/{userId}", testUser.getUserId())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("사용자 검색 - 성공")
    void searchUsers_Success() throws Exception {
        mockMvc.perform(get("/api/users/search")
                        .param("keyword", "test")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("권한별 사용자 조회 - 관리자")
    void getUsersByRole_Admin() throws Exception {
        mockMvc.perform(get("/api/users/role/{role}", UserRole.USER)
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].role").value("USER"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("상태별 사용자 조회 - 관리자")
    void getUsersByStatus_Admin() throws Exception {
        mockMvc.perform(get("/api/users/status/{status}", AccountStatus.ACTIVE)
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
    }

    @Test
    @DisplayName("부서별 사용자 조회 - 성공")
    void getUsersByDepartment_Success() throws Exception {
        mockMvc.perform(get("/api/users/department/{department}", "Computer Science")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].department").value("Computer Science"));
    }

    @Test
    @WithMockUser(username = "1", roles = "USER")
    @DisplayName("친구 코드 재생성 - 본인")
    void regenerateFriendCode_ByOwner() throws Exception {
        mockMvc.perform(post("/api/users/{userId}/friend-code/regenerate", testUser.getUserId())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", matchesPattern("[A-Za-z0-9]{10}")))
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    @WithMockUser(username = "1", roles = "USER")
    @DisplayName("내 친구 코드 재생성 - 성공")
    void regenerateMyFriendCode_Success() throws Exception {
        mockMvc.perform(post("/api/users/me/friend-code/regenerate")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", matchesPattern("[A-Za-z0-9]{10}")))
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    @WithMockUser(username = "1", roles = "USER")
    @DisplayName("내 정보 조회 - 성공")
    void getCurrentUser_Success() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getUserId()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @WithMockUser(username = "1", roles = "USER")
    @DisplayName("내 프로필 조회 - 성공")
    void getCurrentUserProfile_Success() throws Exception {
        mockMvc.perform(get("/api/users/me/profile"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getUserId()))
                .andExpect(jsonPath("$.nickname").value(testUser.getNickname()))
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    private User createTestUser(Long id, String email, String nickname, UserRole role) {
        return User.builder()
                .userId(id)
                .email(email)
                .password("encodedPassword")
                .nickname(nickname)
                .department("Computer Science")
                .studentId("2023000" + id)
                .nationality("Korea")
                .language("Korean")
                .profileImageUrl("http://example.com/profile.jpg")
                .friendCode("TEST" + String.format("%06d", id))
                .role(role)
                .status(AccountStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }
}
