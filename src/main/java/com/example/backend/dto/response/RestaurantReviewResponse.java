package com.example.backend.dto.response;

import com.example.backend.entity.RestaurantReview;
import com.example.backend.entity.enums.RatingBadReason;
import com.example.backend.entity.enums.RatingGoodReason;
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
    private RatingGoodReason ratingGoodReason;
    private RatingBadReason ratingBadReason;
    private String ratingOtherReason;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static RestaurantReviewResponse fromEntity(RestaurantReview restaurantReview) {
        return new RestaurantReviewResponse(
            restaurantReview.getRestaurantReviewId(),
            restaurantReview.getRestaurant().getRestaurantId(),
            restaurantReview.getAuthor().getUserId(),
            restaurantReview.getRating(),
            restaurantReview.getRatingGoodReason(),
            restaurantReview.getRatingBadReason(),
            restaurantReview.getRatingOtherReason(),
            restaurantReview.getCreatedAt(),
            restaurantReview.getUpdatedAt()
        );
    }
}
