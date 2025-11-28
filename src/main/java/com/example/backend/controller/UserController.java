package com.example.backend.controller;

import com.example.backend.dto.request.EmailLoginRequest;
import com.example.backend.dto.request.EmailSignupRequest;
import com.example.backend.dto.response.EmailLoginResponse;
import com.example.backend.dto.response.EmailSignupResponse;
import com.example.backend.entity.User;
import com.example.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/user")
@Tag(name = "User", description = "User API")
@Validated
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup/email")
    @Operation(
        summary = "Sign up a new user",
        description = "Sign up a new user",
        requestBody = @RequestBody(
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
        requestBody = @RequestBody(
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
            if (e.getMessage().contains("not active")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
