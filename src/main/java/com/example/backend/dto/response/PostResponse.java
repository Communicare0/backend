package com.example.backend.dto.response;

import com.example.backend.entity.Post;
import com.example.backend.entity.enums.PostCategory;
import com.example.backend.entity.enums.PostStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
  @NotNull
  private UUID postId;

  @NotNull
  private UUID userId;

  private String title;

  private String content;

  private PostCategory category;

  private boolean isTranslated;

  @NotNull
  private PostStatus status;

  @NotNull
  private int viewCount;

  @NotNull
  private int likeCount;

  @NotNull
  private OffsetDateTime createdAt;

  @NotNull
  private OffsetDateTime updatedAt;

  public static PostResponse fromEntity(Post post) {
    PostResponse response = new PostResponse();
    response.setPostId(post.getPostId());
    response.setUserId(post.getAuthor().getUserId());
    response.setTitle(post.getTitle());
    response.setContent(post.getContent());
    response.setCategory(post.getCategory());
    response.setTranslated(post.isTranslated());
    response.setStatus(post.getStatus());
    response.setViewCount(post.getViewCount());
    response.setLikeCount(post.getLikeCount());
    response.setCreatedAt(post.getCreatedAt());
    response.setUpdatedAt(post.getUpdatedAt());
    return response;
  }
}
