package com.example.backend.service;

import com.example.backend.dto.request.CreateRestaurantReviewRequest;
import com.example.backend.dto.request.UpdateRestaurantReviewRequest;
import com.example.backend.entity.RestaurantReview;

import java.util.List;
import java.util.UUID;

public interface RestaurantReviewService {

    RestaurantReview getReviewById(UUID id);

    List<RestaurantReview> findReviewsByRestaurantId(UUID restaurantId);
    List<RestaurantReview> findReviewsByUserId(UUID userId);

    RestaurantReview createReview(UUID userId, CreateRestaurantReviewRequest request);
    RestaurantReview updateReview(UUID userId, UUID reviewId, UpdateRestaurantReviewRequest request);
    void deleteReview(UUID userId, UUID reviewId);
}
