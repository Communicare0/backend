package com.example.backend.service;

import com.example.backend.dto.CreateUserDto;
import com.example.backend.dto.UpdateUserDto;
import com.example.backend.dto.UserDto;
import com.example.backend.entity.User;
import com.example.backend.entity.enums.AccountStatus;
import com.example.backend.entity.enums.UserRole;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDto createUser(CreateUserDto createUserDto) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(createUserDto.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + createUserDto.getEmail());
        }

        // 닉네임 중복 확인
        if (userRepository.existsByNickname(createUserDto.getNickname())) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다: " + createUserDto.getNickname());
        }

        User user = User.builder()
                .userId(UUID.randomUUID())
                .email(createUserDto.getEmail())
                .password(passwordEncoder.encode(createUserDto.getPassword()))
                .nickname(createUserDto.getNickname())
                .department(createUserDto.getDepartment())
                .studentId(createUserDto.getStudentId())
                .nationality(createUserDto.getNationality())
                .language(createUserDto.getLanguage())
                .profileImageUrl(createUserDto.getProfileImageUrl())
                .friendCode(generateUniqueFriendCode())
                .role(createUserDto.getRole() != null ? createUserDto.getRole() : UserRole.USER)
                .status(AccountStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        log.info("새로운 사용자 생성 완료: {}", savedUser.getEmail());

        return convertToDto(savedUser);
    }

    @Override
    public Optional<UserDto> getUserById(UUID userId) {
        return userRepository.findById(userId)
                .map(this::convertToDto);
    }

    @Override
    public Optional<UserDto> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToDto);
    }

    @Override
    public Optional<UserDto> getUserByFriendCode(String friendCode) {
        return userRepository.findByFriendCode(friendCode)
                .map(this::convertToDto);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAllActiveUsers()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto updateUser(UUID userId, UpdateUserDto updateUserDto) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // 이메일 변경 시 중복 확인
        if (updateUserDto.getEmail() != null && !updateUserDto.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(updateUserDto.getEmail())) {
                throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + updateUserDto.getEmail());
            }
            existingUser.setEmail(updateUserDto.getEmail());
        }

        // 닉네임 변경 시 중복 확인
        if (updateUserDto.getNickname() != null && !updateUserDto.getNickname().equals(existingUser.getNickname())) {
            if (userRepository.existsByNickname(updateUserDto.getNickname())) {
                throw new IllegalArgumentException("이미 존재하는 닉네임입니다: " + updateUserDto.getNickname());
            }
            existingUser.setNickname(updateUserDto.getNickname());
        }

        // 다른 필드들 업데이트
        if (updateUserDto.getDepartment() != null) {
            existingUser.setDepartment(updateUserDto.getDepartment());
        }
        if (updateUserDto.getStudentId() != null) {
            existingUser.setStudentId(updateUserDto.getStudentId());
        }
        if (updateUserDto.getNationality() != null) {
            existingUser.setNationality(updateUserDto.getNationality());
        }
        if (updateUserDto.getLanguage() != null) {
            existingUser.setLanguage(updateUserDto.getLanguage());
        }
        if (updateUserDto.getProfileImageUrl() != null) {
            existingUser.setProfileImageUrl(updateUserDto.getProfileImageUrl());
        }
        if (updateUserDto.getRole() != null) {
            existingUser.setRole(updateUserDto.getRole());
        }
        if (updateUserDto.getStatus() != null) {
            existingUser.setStatus(updateUserDto.getStatus());
        }

        existingUser.setUpdatedAt(OffsetDateTime.now());

        User updatedUser = userRepository.save(existingUser);
        log.info("사용자 정보 업데이트 완료: {}", updatedUser.getUserId());

        return convertToDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        user.setStatus(AccountStatus.DELETED);
        user.setUpdatedAt(OffsetDateTime.now());

        userRepository.save(user);
        log.info("사용자 삭제 완료 (Soft Delete): {}", userId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    @Override
    public boolean existsByFriendCode(String friendCode) {
        return userRepository.existsByFriendCode(friendCode);
    }

    @Override
    public List<UserDto> searchUsers(String keyword, AccountStatus status, int page, int size) {
        int offset = page * size;
        AccountStatus searchStatus = status != null ? status : AccountStatus.ACTIVE;

        return userRepository.findByKeywordAndStatus(keyword, searchStatus)
                .stream()
                .skip(offset)
                .limit(size)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDto> getUsersByDepartment(String department) {
        return userRepository.findByDepartment(department)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDto> getUsersByStatus(AccountStatus status) {
        return userRepository.findByStatus(status)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Long getUserCount() {
        return userRepository.count();
    }

    @Override
    public Long getUserCountByStatus(AccountStatus status) {
        return userRepository.countUsersByStatus(status);
    }

    @Override
    public boolean validatePassword(String email, String password) {
        return userRepository.findByEmail(email)
                .map(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(false);
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(OffsetDateTime.now());

        userRepository.save(user);
        log.info("비밀번호 변경 완료: {}", userId);
    }

    @Override
    public String generateFriendCode() {
        return generateUniqueFriendCode();
    }

    @Override
    public boolean isFriendCodeAvailable(String friendCode) {
        return !userRepository.existsByFriendCode(friendCode);
    }

    // 비공개 메서드들
    private String generateUniqueFriendCode() {
        Random random = new Random();
        String friendCode;
        int attempts = 0;
        final int MAX_ATTEMPTS = 100;

        do {
            friendCode = generateRandomFriendCode(random);
            attempts++;
            if (attempts >= MAX_ATTEMPTS) {
                throw new RuntimeException("친구 코드 생성에 실패했습니다. 다시 시도해주세요.");
            }
        } while (userRepository.existsByFriendCode(friendCode));

        return friendCode;
    }

    private String generateRandomFriendCode(Random random) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private UserDto convertToDto(User user) {
        return UserDto.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .department(user.getDepartment())
                .studentId(user.getStudentId())
                .nationality(user.getNationality())
                .language(user.getLanguage())
                .profileImageUrl(user.getProfileImageUrl())
                .friendCode(user.getFriendCode())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}