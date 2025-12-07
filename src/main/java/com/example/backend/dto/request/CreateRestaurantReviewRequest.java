package com.example.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRestaurantReviewRequest {
    private UUID restaurantId;         // 어떤 레스토랑에 리뷰를 다는지
    private Integer rating;            // 1~5 평점
    private String reason;             // 리뷰 이유
}