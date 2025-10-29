package com.example.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_keywords",
  uniqueConstraints = {
    @UniqueConstraint(name="uk_user_keyword", columnNames = {"user_id","keyword"})
  },
  indexes = {
    @Index(name="ix_keyword_user", columnList = "user_id")
  })
public class UserKeyword {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="keyword_id")
  private Long keywordId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name="user_id", nullable=false,
    foreignKey=@ForeignKey(name="fk_keyword_user"))
  private User user;

  @Column(length = 50, nullable = false)
  private String keyword;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  @Column(nullable = false)
  private OffsetDateTime updatedAt;
  // getters/setters ...
}
