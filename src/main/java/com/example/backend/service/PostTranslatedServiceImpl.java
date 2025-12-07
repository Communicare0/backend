package com.example.backend.service;

import com.example.backend.dto.request.CreatePostTranslatedRequest;
import com.example.backend.dto.request.ExternalCreateTranslationRequest;
import com.example.backend.dto.response.ExternalCreateTranslationResponse;
import com.example.backend.entity.Post;
import com.example.backend.entity.PostTranslated;
import com.example.backend.repository.PostRepository;
import com.example.backend.repository.PostTranslatedRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Objects;
import java.util.UUID;

@Service
public class PostTranslatedServiceImpl implements PostTranslatedService {
    private final PostTranslatedRepository postTranslatedRepository;
    private final PostRepository postRepository;
    private final RestClient restClient;

    public PostTranslatedServiceImpl(
            PostTranslatedRepository postTranslatedRepository,
            PostRepository postRepository,
            RestClient restClient
    ) {
        this.postTranslatedRepository = postTranslatedRepository;
        this.postRepository = postRepository;
        this.restClient = restClient;
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

        String title = null;
        String content = null;
        if (post != null) {
            title = post.getTitle();
        }
        if (post != null) {
            content = post.getContent();
        }

        String translatedTitle = null;
        String translatedContent = null;
        ExternalCreateTranslationResponse response = new ExternalCreateTranslationResponse();
        try {
            if (title != null) {
                ExternalCreateTranslationRequest createTranslationRequest = new ExternalCreateTranslationRequest();
                createTranslationRequest.setSource_lang(""); // source는 알기 어렵네용...
                createTranslationRequest.setTarget_lang(String.valueOf(request.getLanguage()));
                createTranslationRequest.setText(title);
                response = restClient.post()
                        .uri("/api/translate")
                        .body(createTranslationRequest) // JSON으로 직렬화
                        .retrieve()
                        .body(ExternalCreateTranslationResponse.class);
                if (response != null) {
                    translatedTitle = response.getTranslated();
                }
            }
            if (content != null) {
                ExternalCreateTranslationRequest createTranslationRequest = new ExternalCreateTranslationRequest();
                createTranslationRequest.setSource_lang("");
                createTranslationRequest.setTarget_lang(String.valueOf(request.getLanguage()));
                createTranslationRequest.setText(content);
                response = restClient.post()
                        .uri("/api/translate")
                        .body(createTranslationRequest) // JSON으로 직렬화
                        .retrieve()
                        .body(ExternalCreateTranslationResponse.class);
                if (response != null) {
                    translatedContent = response.getTranslated();
                }
            }

        } catch (RestClientResponseException ex) {
            throw new RuntimeException("외부 사용자 생성 실패", ex);
        }

        postTranslated.setTranslatedTitle(Objects.requireNonNullElse(translatedTitle, "번역 불가한 항목입니다."));

        postTranslated.setTranslatedContent(Objects.requireNonNullElse(translatedContent, "번역 불가한 항목입니다."));

        return postTranslated; // 레포에 저장하지 않음. 일단 매번 번역하는것으로
    }
}
