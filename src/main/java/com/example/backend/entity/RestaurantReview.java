package com.example.backend.entity;

import com.example.backend.entity.enums.*;
import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "restaurant_reviews", schema = "communicare",
    indexes = {
        @Index(name="ix_review_restaurant", columnList = "restaurant_id"),
        @Index(name="ix_review_author", columnList = "author_id")
    })
public class RestaurantReview {
    @Id
    @Column(name="restaurant_review_id", columnDefinition = "uuid")
    private UUID restaurantReviewId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="restaurant_id", nullable=false,
    foreignKey=@ForeignKey(name="fk_review_restaurant"))
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="author_id", nullable=false,
    foreignKey=@ForeignKey(name="fk_review_author"))
    private User author;

    @Column(nullable = false)
    private Integer rating; // 1~5

    @Column(columnDefinition = "text")
    private String reason;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    private OffsetDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (restaurantReviewId == null) {
            restaurantReviewId = UUID.randomUUID();
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
