package com.example.backend.entity;

import com.example.backend.entity.enums.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
@Data
@Entity
@Table(name = "restaurants", schema = "communicare")
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {
    @Id
    @Column(name="restaurant_id", columnDefinition = "uuid")
    private UUID restaurantId;

    @Column(length = 255, nullable = false)
    private String name;

    @Column(length = 255, nullable = false)
    private String googleMapUrl;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(
        nullable = false,
        name = "status",
        columnDefinition = "communicare.restaurant_status"
    )
    @Builder.Default
    private RestaurantStatus status = RestaurantStatus.VISIBLE;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(
        nullable = false,
        name = "restaurant_type",
        columnDefinition = "communicare.restaurant_type"
    )
    @Builder.Default
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
