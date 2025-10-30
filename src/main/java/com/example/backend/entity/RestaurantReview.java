package com.example.backend.entity;

import com.example.backend.entity.enums.*;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "restaurant_reviews", schema = "communicare",
  indexes = {
    @Index(name="ix_review_restaurant", columnList = "restaurant_id"),
    @Index(name="ix_review_author", columnList = "author_id")
  })
public class RestaurantReview {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="restaurant_review_id")
  private Long restaurantReviewId;

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

  @Enumerated(EnumType.STRING)
  private RatingGoodReason ratingGoodReason;

  @Enumerated(EnumType.STRING)
  private RatingBadReason ratingBadReason;

  @Column(columnDefinition = "text")
  private String ratingOtherReason;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  private OffsetDateTime deletedAt;
  // getters/setters ...
}
