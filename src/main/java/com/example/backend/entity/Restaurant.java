package com.example.backend.entity;

import com.example.backend.entity.enums.*;
import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "restaurants", schema = "communicare")
public class Restaurant {
    @Id
    @Column(name="restaurant_id", columnDefinition = "uuid")
    private UUID restaurantId;

    @Column(length = 255, nullable = false)
    private String name;

    @Column(length = 255, nullable = false)
    private String googleMapUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RestaurantStatus status = RestaurantStatus.VISIBLE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RestaurantType restaurantType = RestaurantType.NONE;

    private Integer ratingCount;
    private Integer ratingSum;

    @Column(precision = 3, scale = 2)
    private java.math.BigDecimal avgRating;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    private OffsetDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (restaurantId == null) {
            restaurantId = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
