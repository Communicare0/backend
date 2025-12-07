package com.example.backend.dto.response;

import com.example.backend.entity.enums.Language;
import com.example.backend.entity.enums.Nationality;
import com.example.backend.entity.enums.PreferredFoodType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeProfileResponse {

    private String department;             // 학과
    private String studentId;              // 학번
    private Nationality nationality;       // 국적
    private PreferredFoodType preferredFoodType; // 선호 음식
    private Language language;             // 언어
}