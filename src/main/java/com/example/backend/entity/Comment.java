package com.example.backend.entity;

import com.example.backend.entity.enums.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

/** 3. Comment */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "comments", schema = "communicare",
  indexes = {
    @Index(name="ix_comment_post", columnList = "post_id"),
    @Index(name="ix_comment_author", columnList = "author_id"),
    @Index(name="ix_comment_parent", columnList = "parent_id")
  })
public class Comment {
  @Id
  @Column(name="comment_id", columnDefinition = "uuid")
  private UUID commentId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name="post_id", nullable=false,
    foreignKey=@ForeignKey(name="fk_comment_post"))
  private Post post;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name="author_id", nullable=false,
    foreignKey=@ForeignKey(name="fk_comment_author"))
  private User author;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name="parent_id", foreignKey=@ForeignKey(name="fk_comment_parent"))
  private Comment parent;

  @Column(length = 255, nullable = false)
  private String content;

  @Column(nullable = false)
  private boolean isTranslated = false;

  @Enumerated(EnumType.STRING)
  private CommentStatus status = CommentStatus.VISIBLE;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  private OffsetDateTime deletedAt;
  // getters/setters ...
 
}
