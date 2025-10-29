package com.example.backend.entity;

import com.example.backend.entity.enums.*;
import jakarta.persistence.*;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

/** 4. Post */
@Entity
@Table(name = "posts",
  indexes = {
    @Index(name="ix_post_author", columnList = "author_id")
  })
public class Post {
  @Id
  @Column(name="post_id", columnDefinition = "uuid")
  private UUID postId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name="author_id", nullable=false,
    foreignKey=@ForeignKey(name="fk_post_author"))
  private User author;

  @Column(columnDefinition = "text")
  private String title;

  @Column(columnDefinition = "text")
  private String content;

  @Enumerated(EnumType.STRING)
  private PostCategory category;

  @Column(nullable = false)
  private boolean isTranslated = false;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PostStatus status = PostStatus.VISIBLE;

  @Column(nullable = false)
  private int viewCount = 0;

  @Column(nullable = false)
  private int likeCount = 0;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  private OffsetDateTime deletedAt;
  // getters/setters ...
}
