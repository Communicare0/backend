package com.example.backend.dto;

import com.example.backend.entity.enums.AccountStatus;
import com.example.backend.entity.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID userId;
    private String email;
    private String nickname;
    private String department;
    private String studentId;
    private String nationality;
    private String language;
    private String profileImageUrl;
    private String friendCode;
    private UserRole role;
    private AccountStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}