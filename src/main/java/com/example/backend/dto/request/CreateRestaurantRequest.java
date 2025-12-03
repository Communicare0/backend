package com.example.backend.dto.request;

import com.example.backend.entity.enums.RestaurantType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "레스토랑 생성 요청")
public class CreateRestaurantRequest {
    @NotBlank(message = "레스토랑 이름은 필수입니다.")
    @Schema(description = "레스토랑 이름", example = "마라탕 전문점")
    private String name;

    @Schema(description = "구글 지도 URL", example = "https://maps.google.com/?q=...")
    private String googleMapUrl;

    @NotNull(message = "레스토랑 타입은 필수입니다.")
    @Schema(description = "레스토랑 타입", allowableValues = {"HALAL", "KOSHER", "VEGAN", "NONE"})
    private RestaurantType restaurantType;
}