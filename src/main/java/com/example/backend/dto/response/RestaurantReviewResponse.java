package com.example.backend.dto.response;

import com.example.backend.entity.RestaurantReview;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantReviewResponse {
    private UUID restaurantReviewId;
    private UUID restaurantId;
    private UUID authorId;
    private Integer rating;
    private String reason;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static RestaurantReviewResponse fromEntity(RestaurantReview restaurantReview) {
        return new RestaurantReviewResponse(
            restaurantReview.getRestaurantReviewId(),
            restaurantReview.getRestaurant().getRestaurantId(),
            restaurantReview.getAuthor().getUserId(),
            restaurantReview.getRating(),
            restaurantReview.getReason(),
            restaurantReview.getCreatedAt(),
            restaurantReview.getUpdatedAt()
        );
    }
}
