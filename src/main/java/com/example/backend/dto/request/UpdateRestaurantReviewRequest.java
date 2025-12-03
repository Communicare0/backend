package com.example.backend.dto.request;

import com.example.backend.entity.enums.RatingGoodReason;
import com.example.backend.entity.enums.RatingBadReason;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRestaurantReviewRequest {
    private Integer rating;            // 1~5 평점
    private RatingGoodReason ratingGoodReason;  // 긍정 평가 이유
    private RatingBadReason ratingBadReason;    // 부정 평가 이유
    private String ratingOtherReason;   // 기타 이유
}