package com.example.backend.controller;

import com.example.backend.dto.request.UpdateUserPreferencesRequest;
import com.example.backend.dto.request.UpdateUserProfileRequest;
import com.example.backend.dto.response.UserPreferencesResponse;
import com.example.backend.dto.response.FriendCodeResponse;
import com.example.backend.dto.response.MeProfileResponse;
import com.example.backend.service.UserPreferenceService;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/me")
@RequiredArgsConstructor
public class MeController {

    private final UserPreferenceService userPreferenceService;
    private final UserRepository userRepository;

    @PostMapping("/preferences")
    public ResponseEntity<UserPreferencesResponse> updateMyPreferences(
            @RequestBody UpdateUserPreferencesRequest request
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String userIdStr = authentication.getName();  // 보통 subject 들어감
        UUID userId = UUID.fromString(userIdStr);

        UserPreferencesResponse response =
                userPreferenceService.updateUserPreferences(userId, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/friend-code")
    public ResponseEntity<FriendCodeResponse> getMyFriendCode() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String userIdStr = authentication.getName();
        UUID userId = UUID.fromString(userIdStr);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        FriendCodeResponse response = new FriendCodeResponse(user.getFriendCode());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<MeProfileResponse> getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String userIdStr = authentication.getName();   // JWT subject = userId (UUID)
        UUID userId = UUID.fromString(userIdStr);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        MeProfileResponse response = MeProfileResponse.builder()
                .department(user.getDepartment())
                .studentId(user.getStudentId())
                .nationality(user.getNationality())
                .preferredFoodType(user.getPreferredFoodType())
                .language(user.getLanguage())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/profile")
    public ResponseEntity<MeProfileResponse> updateMyProfile(
        @RequestBody UpdateUserProfileRequest request
    ) {
    
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    String userIdStr = authentication.getName();   // JWT subject = userId (UUID)
    UUID userId = UUID.fromString(userIdStr);

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    // 값이 들어온 것만 업데이트
    if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment());
    }
    if (request.getStudentId() != null) {
            user.setStudentId(request.getStudentId());
    }

    userRepository.save(user);

    MeProfileResponse response = MeProfileResponse.builder()
            .department(user.getDepartment())
            .studentId(user.getStudentId())
            .nationality(user.getNationality())
            .preferredFoodType(user.getPreferredFoodType())
            .language(user.getLanguage())
            .build();

    return ResponseEntity.ok(response);
    }
}