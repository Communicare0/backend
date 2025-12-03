package com.example.backend.repository;

import com.example.backend.entity.Restaurant;
import com.example.backend.entity.enums.RestaurantStatus;
import com.example.backend.entity.enums.RestaurantType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    @Query("SELECT r FROM Restaurant r WHERE r.status = :status ORDER BY " +
           "CASE WHEN r.restaurantType = :preferredType THEN 0 ELSE 1 END, " +
           "COALESCE(r.avgRating, 0) DESC, r.createdAt DESC")
    List<Restaurant> findByRecommendedOrder(
            @Param("status") RestaurantStatus status,
            @Param("preferredType") RestaurantType preferredType
    );
}
