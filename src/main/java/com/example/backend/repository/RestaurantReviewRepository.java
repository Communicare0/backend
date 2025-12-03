package com.example.backend.repository;

import com.example.backend.entity.RestaurantReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RestaurantReviewRepository extends JpaRepository<RestaurantReview, UUID>, RestaurantReviewRepositoryCustom {
    List<RestaurantReview> findByRestaurant_RestaurantIdOrderByCreatedAtDesc(UUID restaurantId);
    List<RestaurantReview> findByAuthor_UserIdOrderByCreatedAtDesc(UUID authorUserId);
    List<RestaurantReview> findByRestaurant_RestaurantIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID restaurantId);
    List<RestaurantReview> findByAuthor_UserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID authorUserId);
}
