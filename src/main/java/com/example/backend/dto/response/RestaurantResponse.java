package com.example.backend.dto.response;

import com.example.backend.entity.Restaurant;
import com.example.backend.entity.enums.RestaurantStatus;
import com.example.backend.entity.enums.RestaurantType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {
    @NotNull
    private UUID restaurantId;

    private String name;

    private String googleMapUrl;

    private RestaurantStatus status;

    private RestaurantType restaurantType;

    private Integer ratingCount;

    private Integer ratingSum;

    private java.math.BigDecimal avgRating;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    public static RestaurantResponse fromEntity(Restaurant restaurant) {
        RestaurantResponse response = new RestaurantResponse();
        response.setRestaurantId(restaurant.getRestaurantId());
        response.setName(restaurant.getName());
        response.setGoogleMapUrl(restaurant.getGoogleMapUrl());
        response.setStatus(restaurant.getStatus());
        response.setRestaurantType(restaurant.getRestaurantType());
        response.setRatingCount(restaurant.getRatingCount());
        response.setRatingSum(restaurant.getRatingSum());
        response.setAvgRating(restaurant.getAvgRating());
        response.setCreatedAt(restaurant.getCreatedAt());
        response.setUpdatedAt(restaurant.getUpdatedAt());
        return response;
    }
}
