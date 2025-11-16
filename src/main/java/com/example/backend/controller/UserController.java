package com.example.backend.controller;

import com.example.backend.dto.CreateUserDto;
import com.example.backend.dto.UpdateUserDto;
import com.example.backend.dto.UserDto;
import com.example.backend.entity.enums.AccountStatus;
import com.example.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User API", description = "사용자 관리 API")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "사용자 생성", description = "새로운 사용자를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "사용자 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "409", description = "이메일 또는 닉네임 중복")
    })
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserDto createUserDto) {
        UserDto createdUser = userService.createUser(createUserDto);
        log.info("사용자 생성 API 호출: {}", createdUser.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "사용자 조회", description = "ID로 특정 사용자를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<UserDto> getUserById(
            @Parameter(description = "사용자 ID", required = true) @PathVariable UUID userId) {
        Optional<UserDto> user = userService.getUserById(userId);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "이메일로 사용자 조회", description = "이메일로 특정 사용자를 조회합니다.")
    public ResponseEntity<UserDto> getUserByEmail(
            @Parameter(description = "사용자 이메일", required = true) @PathVariable String email) {
        Optional<UserDto> user = userService.getUserByEmail(email);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/friend-code/{friendCode}")
    @Operation(summary = "친구 코드로 사용자 조회", description = "친구 코드로 특정 사용자를 조회합니다.")
    public ResponseEntity<UserDto> getUserByFriendCode(
            @Parameter(description = "친구 코드", required = true) @PathVariable String friendCode) {
        Optional<UserDto> user = userService.getUserByFriendCode(friendCode);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "전체 사용자 목록 조회", description = "모든 활성 사용자 목록을 조회합니다.")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}")
    @Operation(summary = "사용자 정보 수정", description = "특정 사용자의 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이메일 또는 닉네임 중복")
    })
    public ResponseEntity<UserDto> updateUser(
            @Parameter(description = "사용자 ID", required = true) @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserDto updateUserDto) {
        UserDto updatedUser = userService.updateUser(userId, updateUserDto);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "사용자 삭제", description = "특정 사용자를 Soft Delete 방식으로 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "사용자 ID", required = true) @PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    // 검색 및 필터링 API
    @GetMapping("/search")
    @Operation(summary = "사용자 검색", description = "키워드로 사용자를 검색합니다.")
    public ResponseEntity<List<UserDto>> searchUsers(
            @Parameter(description = "검색 키워드") @RequestParam(required = false) String keyword,
            @Parameter(description = "계정 상태") @RequestParam(required = false) AccountStatus status,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {
        List<UserDto> users = userService.searchUsers(keyword, status, page, size);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/department/{department}")
    @Operation(summary = "학과별 사용자 조회", description = "특정 학과 소속 사용자 목록을 조회합니다.")
    public ResponseEntity<List<UserDto>> getUsersByDepartment(
            @Parameter(description = "학과", required = true) @PathVariable String department) {
        List<UserDto> users = userService.getUsersByDepartment(department);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "상태별 사용자 조회", description = "특정 상태를 가진 사용자 목록을 조회합니다.")
    public ResponseEntity<List<UserDto>> getUsersByStatus(
            @Parameter(description = "계정 상태", required = true) @PathVariable AccountStatus status) {
        List<UserDto> users = userService.getUsersByStatus(status);
        return ResponseEntity.ok(users);
    }

    // 유효성 검사 API
    @GetMapping("/check/email")
    @Operation(summary = "이메일 중복 확인", description = "이메일 사용 가능 여부를 확인합니다.")
    public ResponseEntity<Map<String, Boolean>> checkEmailAvailability(
            @Parameter(description = "확인할 이메일", required = true) @RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(Map.of("available", !exists));
    }

    @GetMapping("/check/nickname")
    @Operation(summary = "닉네임 중복 확인", description = "닉네임 사용 가능 여부를 확인합니다.")
    public ResponseEntity<Map<String, Boolean>> checkNicknameAvailability(
            @Parameter(description = "확인할 닉네임", required = true) @RequestParam String nickname) {
        boolean exists = userService.existsByNickname(nickname);
        return ResponseEntity.ok(Map.of("available", !exists));
    }

    @GetMapping("/check/friend-code")
    @Operation(summary = "친구 코드 중복 확인", description = "친구 코드 사용 가능 여부를 확인합니다.")
    public ResponseEntity<Map<String, Boolean>> checkFriendCodeAvailability(
            @Parameter(description = "확인할 친구 코드", required = true) @RequestParam String friendCode) {
        boolean available = userService.isFriendCodeAvailable(friendCode);
        return ResponseEntity.ok(Map.of("available", available));
    }

    // 통계 API
    @GetMapping("/stats/count")
    @Operation(summary = "전체 사용자 수 조회", description = "전체 사용자 수를 반환합니다.")
    public ResponseEntity<Map<String, Long>> getUserCount() {
        Long count = userService.getUserCount();
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/stats/count/{status}")
    @Operation(summary = "상태별 사용자 수 조회", description = "특정 상태를 가진 사용자 수를 반환합니다.")
    public ResponseEntity<Map<String, Long>> getUserCountByStatus(
            @Parameter(description = "계정 상태", required = true) @PathVariable AccountStatus status) {
        Long count = userService.getUserCountByStatus(status);
        return ResponseEntity.ok(Map.of("count", count));
    }

    // 비밀번호 관련 API
    @PostMapping("/validate-password")
    @Operation(summary = "비밀번호 확인", description = "이메일과 비밀번호가 일치하는지 확인합니다.")
    public ResponseEntity<Map<String, Boolean>> validatePassword(
            @RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        boolean isValid = userService.validatePassword(email, password);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }

    @PutMapping("/{userId}/password")
    @Operation(summary = "비밀번호 변경", description = "사용자의 비밀번호를 변경합니다.")
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "사용자 ID", required = true) @PathVariable UUID userId,
            @RequestBody Map<String, String> request) {
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");

        userService.changePassword(userId, currentPassword, newPassword);
        return ResponseEntity.ok().build();
    }

    // 친구 코드 생성 API
    @PostMapping("/friend-code/generate")
    @Operation(summary = "친구 코드 생성", description = "새로운 친구 코드를 생성합니다.")
    public ResponseEntity<Map<String, String>> generateFriendCode() {
        String friendCode = userService.generateFriendCode();
        return ResponseEntity.ok(Map.of("friendCode", friendCode));
    }
}