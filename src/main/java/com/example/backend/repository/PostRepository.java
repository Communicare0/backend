package com.example.backend.repository;

import com.example.backend.entity.Post;
import com.example.backend.entity.enums.PostCategory;
import com.example.backend.entity.enums.PostStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID>, PostRepositoryCustom {
  List<Post> findByAuthor_UserId(UUID authorUserId);

  List<Post> findByCategory(PostCategory category);

  List<Post> findByAuthor_UserIdAndStatusAndDeletedAtIsNull(UUID authorUserId, PostStatus status);

  List<Post> findByCategoryAndStatusAndDeletedAtIsNull(PostCategory category, PostStatus status);

  Post findByPostIdAndStatusAndDeletedAtIsNull(UUID postId, PostStatus status);

  void deleteByAuthor_UserId(UUID authorUserId);

  @Query("SELECT p FROM Post p WHERE (p.title ILIKE CONCAT('%', :keyword, '%') OR p.content ILIKE CONCAT('%', :keyword, '%')) " +
         "AND p.status = 'VISIBLE' AND p.deletedAt IS NULL " +
         "ORDER BY p.createdAt DESC")
  List<Post> findByKeyword(String keyword);

  @Query("SELECT p FROM Post p WHERE (p.title ILIKE CONCAT('%', :keyword, '%') OR p.content ILIKE CONCAT('%', :keyword, '%')) " +
         "AND p.category = :category AND p.status = 'VISIBLE' AND p.deletedAt IS NULL " +
         "ORDER BY p.createdAt DESC")
  List<Post> findByKeywordAndCategory(String keyword, PostCategory category);
}
