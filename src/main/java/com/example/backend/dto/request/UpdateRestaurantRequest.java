package com.example.backend.dto.request;

import com.example.backend.entity.enums.RestaurantStatus;
import com.example.backend.entity.enums.RestaurantType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "레스토랑 수정 요청")
public class UpdateRestaurantRequest {
    @NotBlank(message = "레스토랑 이름은 필수입니다.")
    @Schema(description = "레스토랑 이름", example = "수정된 마라탕 전문점")
    private String name;

    @Schema(description = "구글 지도 URL", example = "https://maps.google.com/?q=...")
    private String googleMapUrl;

    @Schema(description = "레스토랑 상태", allowableValues = {"VISIBLE", "DELETED", "BLOCKED"})
    private RestaurantStatus status;

    @Schema(description = "레스토랑 타입", allowableValues = {"HALAL", "KOSHER", "VEGAN", "KOREA", "JAPAN", "CHINA", "VIETNAM", "INDIA", "WEST", "NONE"})
    private RestaurantType restaurantType;

    @Schema(description = "레스토랑 리뷰 개수")
    private Integer ratingCount;

    @Schema(description = "레스토랑 별점 합계")
    private Integer ratingSum;

    @Schema(description = "레스토랑 별점 평균")
    private java.math.BigDecimal avgRating;
}
