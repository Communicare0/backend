package com.example.backend.service;

import com.example.backend.dto.request.CreatePostTranslatedRequest;
import com.example.backend.entity.Post;
import com.example.backend.entity.PostTranslated;
import com.example.backend.repository.PostRepository;
import com.example.backend.repository.PostTranslatedRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PostTranslatedServiceImpl implements PostTranslatedService {
    private final PostTranslatedRepository postTranslatedRepository;
    private final PostRepository postRepository;

    public PostTranslatedServiceImpl(PostTranslatedRepository postTranslatedRepository, PostRepository postRepository) {
        this.postTranslatedRepository = postTranslatedRepository;
        this.postRepository = postRepository;
    }

    @Override
    public PostTranslated getPostTranslatedById(UUID id) {
        return postTranslatedRepository.findById(id).orElse(null);
    }

    @Override
    public PostTranslated getPostTranslatedByPostId(UUID postId) {
        return postTranslatedRepository.findByPost_PostId(postId);
    }

    @Override
    @Transactional
    public PostTranslated createPostTranslated(CreatePostTranslatedRequest request) {
        Post post = postRepository.findById(request.getPostId()).orElse(null);
        PostTranslated postTranslated = new PostTranslated();
        postTranslated.setPost(post);
        postTranslated.setLanguage(request.getLanguage());

//        URL : http://192.168.200.109:5000/api/translate
//        인증 -> key:X-API-Key, value:sksmsdiwlgusdlek57585958754 입니다!


    }
}
