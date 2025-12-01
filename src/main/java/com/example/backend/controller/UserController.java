package com.example.backend.controller;

import com.example.backend.dto.request.EmailLoginRequest;
import com.example.backend.dto.request.EmailSignupRequest;
import com.example.backend.dto.request.UpdateUserRequest;
import com.example.backend.dto.response.EmailLoginResponse;
import com.example.backend.dto.response.EmailSignupResponse;
import com.example.backend.entity.User;
import com.example.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
// import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@RestController
@RequestMapping("/v1/user")
@Tag(name = "User", description = "User API")
@Validated
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication != null ? authentication.getPrincipal() : null;

        if (principal instanceof UUID uuid) {
            return uuid;
        }

        throw new IllegalStateException("인증 정보에서 사용자 ID를 찾을 수 없습니다.");
    }

    @PostMapping("/signup/email")
    @Operation(
    summary = "Sign up a new user",
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "email, password, nickname",
        required = true,
        content = @Content(schema = @Schema(implementation = EmailSignupRequest.class))
    ), 
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Successfully signup",
                content = @Content(schema = @Schema(implementation = EmailSignupResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Bad request - Invalid input"
            ),
            @ApiResponse(
                responseCode = "409",
                description = "Conflict - Email or nickname already exists"
            )
        }
    )
    public ResponseEntity<EmailSignupResponse> signupWithEmail(@Valid @RequestBody EmailSignupRequest request) {
        try {
            User user = userService.signupWithEmail(request);
            EmailSignupResponse response = EmailSignupResponse.fromEntity(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/login/email")
    @Operation(
        summary = "이메일 로그인",
        description = "이메일과 비밀번호로 로그인합니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(  // 👈 여기
            description = "email, password",
            required = true,
            content = @Content(schema = @Schema(implementation = EmailLoginRequest.class))
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully login",
                content = @Content(schema = @Schema(implementation = EmailLoginResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Bad request - Invalid credentials"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - Account not active"
            )
        }
    )
    public ResponseEntity<EmailLoginResponse> loginWithEmail(@Valid @RequestBody EmailLoginRequest request) {
        try {
            EmailLoginResponse response = userService.loginWithEmail(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            e.printStackTrace(); // 또는 log.error("Login error", e);

            String msg = e.getMessage(); // 디버깅용 추가

            if (e.getMessage().contains("not active")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            e.printStackTrace(); // 디버깅용
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/update")
    @Operation(
        summary = "Update current user information",
        description = "Update department, studentId, nationality, preferredFoodType, language for current authenticated user",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "User information to update (all fields are optional)",
            required = true,
            content = @Content(schema = @Schema(implementation = UpdateUserRequest.class))
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "User information updated successfully",
                content = @Content(schema = @Schema(implementation = User.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Bad request - Invalid input"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized - Authentication required"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "User not found"
            )
        }
    )
    public ResponseEntity<User> updateUser(
            @Parameter(description = "User information to update", required = true)
            @Valid @RequestBody UpdateUserRequest request) {
        try {
            UUID userId = getCurrentUserId();
            User updatedUser = userService.updateUser(userId, request);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
