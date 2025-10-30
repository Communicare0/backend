package com.example.backend.entity;

import com.example.backend.entity.enums.*;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "restaurants", schema = "communicare")
public class Restaurant {
  @Id
  @Column(name="restaurant_id", columnDefinition = "uuid")
  private UUID restaurantId;

  @Column(length = 255, nullable = false)
  private String name;

  @Column(name="name_localized", columnDefinition = "jsonb")
  private String nameLocalized; // JSON 문자열 저장(예: {"ko":"아주캠프","en":"Ajou Camp"})

  @Column(length = 255)
  private String photoUrl;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RestaurantStatus status = RestaurantStatus.VISIBLE;

  @Enumerated(EnumType.STRING)
  private RestaurantType restaurantType;

  private Integer ratingCount;
  private Integer ratingSum;

  @Column(precision = 3, scale = 2)
  private java.math.BigDecimal avgRating;

  @Column(length = 256)
  private String address;

  @Column(length = 16)
  private String phone;

  @Column(length = 256)
  private String website;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  private OffsetDateTime deletedAt;
  // getters/setters ...
}
