package com.example.backend.dto.response;

import com.example.backend.entity.Post;
import com.example.backend.entity.enums.Nationality;
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

  private String authorDepartment;   // 작성자 학과
  private String authorStudentYear;  // 예: "21학번"
  private Nationality authorNationality; // 작성자 국적

  private UUID authorId;

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

 private static String maskStudentIdToYear(String studentId) {
    if (studentId == null) {
      return null;
    }

    if (studentId.length() >= 4) {
      try {
        String yearStr = studentId.substring(0, 4);   // ex: "2021"
        int year = Integer.parseInt(yearStr);
        int shortYear = year % 100;                   // ex: 21
        return shortYear + "학번";
      } catch (NumberFormatException e) {
        return null;
      }
    }

    return null;
  } 

  public static PostResponse fromEntity(Post post) {
    PostResponse response = new PostResponse();
    
    // 작성자 정보
    if (post.getAuthor() != null) {
      response.setAuthorDepartment(post.getAuthor().getDepartment());
      response.setAuthorStudentYear(maskStudentIdToYear(post.getAuthor().getStudentId()));
      response.setAuthorNationality(post.getAuthor().getNationality());
    }
    
    response.setPostId(post.getPostId());
    response.setTitle(post.getTitle());
    response.setContent(post.getContent());
    response.setCategory(post.getCategory());
    response.setTranslated(post.isTranslated());
    response.setStatus(post.getStatus());
    response.setViewCount(post.getViewCount());
    response.setLikeCount(post.getLikeCount());
    response.setCreatedAt(post.getCreatedAt());
    response.setUpdatedAt(post.getUpdatedAt());
    response.setAuthorId(post.getAuthor().getUserId());
    return response;
  }
}
