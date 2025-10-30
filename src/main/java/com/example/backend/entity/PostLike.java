package com.example.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
  name = "post_likes", schema = "communicare",
  uniqueConstraints = {
    @UniqueConstraint(name = "uk_post_like_user_post", columnNames = {"user_id", "post_id"})
  },
  indexes = {
    @Index(name = "ix_post_like_user", columnList = "user_id"),
    @Index(name = "ix_post_like_post", columnList = "post_id")
  }
)
public class PostLike {

  /** 대체키(단일 PK) */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "post_like_id")
  private Long postLikeId;

  /** 좋아요를 누른 사용자 */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false,
    foreignKey = @ForeignKey(name = "fk_post_like_user"))
  private User user;

  /** 좋아요 대상 게시글 (UUID) */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "post_id", nullable = false,
    foreignKey = @ForeignKey(name = "fk_post_like_post"))
  private Post post;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  private OffsetDateTime deletedAt;

  // === getters / setters ===
  public Long getPostLikeId() { return postLikeId; }
  public void setPostLikeId(Long postLikeId) { this.postLikeId = postLikeId; }
  public User getUser() { return user; }
  public void setUser(User user) { this.user = user; }
  public Post getPost() { return post; }
  public void setPost(Post post) { this.post = post; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
  public OffsetDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
  public OffsetDateTime getDeletedAt() { return deletedAt; }
  public void setDeletedAt(OffsetDateTime deletedAt) { this.deletedAt = deletedAt; }

  // equals/hashCode는 기본키 기반(영속 후) 또는 user+post 복합키 기반 중 하나로 선택해서 구현하세요.
}
