package com.example.backend.dto.request;

import com.example.backend.entity.enums.Language;
import com.example.backend.entity.enums.Nationality;
import com.example.backend.entity.enums.PreferredFoodType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 정보 수정 요청")
public class UpdateUserRequest {
    @Schema(description = "학과", example = "컴퓨터공학과")
    private String department;

    @Schema(description = "학번", example = "20230001")
    private String studentId;

    @Schema(description = "국적", allowableValues = {"KOREAN", "VIETNAMESE", "CHINESE", "MYANMARESE", "JAPANESE", "INDONESIAN", "MALAYSIAN", "EMIRATIS"})
    private Nationality nationality;

    @Schema(description = "선호 음식 타입", allowableValues = {"HALAL", "KOSHER", "VEGAN", "NONE"})
    private PreferredFoodType preferredFoodType;

    @Schema(description = "언어", allowableValues = {"KO", "EN", "ZH", "JA", "ES", "FR", "DE", "RU", "AR", "OTHER"})
    private Language language;
}