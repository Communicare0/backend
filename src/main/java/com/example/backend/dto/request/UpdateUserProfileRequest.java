package com.example.backend.dto.request;

import lombok.Data;

@Data
public class UpdateUserProfileRequest {
    private String department;  // 변경할 학과
    private String studentId;   // 변경할 학번
}