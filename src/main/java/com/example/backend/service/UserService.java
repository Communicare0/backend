package com.example.backend.service;

import com.example.backend.dto.CreateUserDto;
import com.example.backend.dto.UpdateUserDto;
import com.example.backend.dto.UserDto;
import com.example.backend.entity.enums.AccountStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    // CRUD 기본 기능
    UserDto createUser(CreateUserDto createUserDto);
    Optional<UserDto> getUserById(UUID userId);
    Optional<UserDto> getUserByEmail(String email);
    Optional<UserDto> getUserByFriendCode(String friendCode);
    List<UserDto> getAllUsers();
    UserDto updateUser(UUID userId, UpdateUserDto updateUserDto);
    void deleteUser(UUID userId); // Soft delete

    // 비즈니스 로직 관련 기능
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    boolean existsByFriendCode(String friendCode);

    // 검색 및 페이징 기능
    List<UserDto> searchUsers(String keyword, AccountStatus status, int page, int size);
    List<UserDto> getUsersByDepartment(String department);
    List<UserDto> getUsersByStatus(AccountStatus status);

    // 통계 기능
    Long getUserCount();
    Long getUserCountByStatus(AccountStatus status);

    // 비밀번호 관련 기능
    boolean validatePassword(String email, String password);
    void changePassword(UUID userId, String currentPassword, String newPassword);

    // 친구 코드 관련 기능
    String generateFriendCode();
    boolean isFriendCodeAvailable(String friendCode);
}
