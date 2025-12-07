package com.example.backend.service;

import com.example.backend.dto.request.UpdateUserPreferencesRequest;
import com.example.backend.dto.response.UserPreferencesResponse;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private final UserRepository userRepository;

    @Transactional
    public UserPreferencesResponse updateUserPreferences(
            UUID userId,
            UpdateUserPreferencesRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getNationality() != null) {
            user.setNationality(request.getNationality());
        }

        if (request.getLanguage() != null) {
            user.setLanguage(request.getLanguage());
        }

        if (request.getPreferredFoodType() != null) {
            user.setPreferredFoodType(request.getPreferredFoodType());
        }

        return UserPreferencesResponse.builder()
                .nationality(user.getNationality())
                .language(user.getLanguage())
                .preferredFoodType(user.getPreferredFoodType())
                .build();
    }
}