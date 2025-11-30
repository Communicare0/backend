package com.example.backend.repository;

import com.example.backend.entity.Post;
import com.example.backend.entity.enums.PostCategory;
import com.example.backend.entity.enums.PostStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID>, PostRepositoryCustom {
  List<Post> findByAuthor_UserId(UUID authorUserId);

  List<Post> findByCategory(PostCategory category);

//  Post createPost(Post post);
//
//  Post updatePost(Post post);

  List<Post> findByAuthor_UserIdAndStatusAndDeletedAtIsNull(UUID authorUserId, PostStatus status);

  List<Post> findByCategoryAndStatusAndDeletedAtIsNull(PostCategory category, PostStatus status);

  Post findByPostIdAndStatusAndDeletedAtIsNull(UUID postId, PostStatus status);

  void deleteByAuthor_UserId(UUID authorUserId);
}
