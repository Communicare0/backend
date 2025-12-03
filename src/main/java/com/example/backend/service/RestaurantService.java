package com.example.backend.service;

import com.example.backend.dto.request.CreateRestaurantRequest;
import com.example.backend.dto.request.UpdateRestaurantRequest;
import com.example.backend.dto.response.RestaurantResponse;
import com.example.backend.entity.Restaurant;

import java.util.List;
import java.util.UUID;

public interface RestaurantService {
    List<RestaurantResponse> getAllRestaurants();
    RestaurantResponse getRestaurantById(UUID restaurantId);
    RestaurantResponse createRestaurant(CreateRestaurantRequest request);
    RestaurantResponse updateRestaurant(UUID restaurantId, UpdateRestaurantRequest request);
    void deleteRestaurant(UUID restaurantId);
    List<RestaurantResponse> getRecommendedRestaurants(UUID userId);
}
