package com.example.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRestaurantReviewRequest {
    private Integer rating;            // 1~5 평점
    private String reason;             // 리뷰 이유
}