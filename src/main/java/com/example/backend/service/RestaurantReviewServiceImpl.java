package com.example.backend.service;

import com.example.backend.dto.request.CreateRestaurantReviewRequest;
import com.example.backend.dto.request.UpdateRestaurantReviewRequest;
import com.example.backend.entity.Restaurant;
import com.example.backend.entity.RestaurantReview;
import com.example.backend.entity.User;
import com.example.backend.repository.RestaurantRepository;
import com.example.backend.repository.RestaurantReviewRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class RestaurantReviewServiceImpl implements RestaurantReviewService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantReviewRepository restaurantReviewRepository;

    public RestaurantReviewServiceImpl(
        UserRepository userRepository,
        RestaurantRepository restaurantRepository,
        RestaurantReviewRepository restaurantReviewRepository
    ) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.restaurantReviewRepository = restaurantReviewRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantReview getReviewById(UUID id) {
        return restaurantReviewRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantReview> findReviewsByRestaurantId(UUID restaurantId) {
        return restaurantReviewRepository.findByRestaurant_RestaurantIdAndDeletedAtIsNullOrderByCreatedAtDesc(restaurantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantReview> findReviewsByUserId(UUID userId) {
        return restaurantReviewRepository.findByAuthor_UserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId);
    }

    @Override
    public RestaurantReview createReview(UUID userId, CreateRestaurantReviewRequest request) {
        User user = userRepository.getReferenceById(userId);
        Restaurant restaurant = restaurantRepository.getReferenceById(request.getRestaurantId());

        RestaurantReview review = new RestaurantReview();
        review.setRestaurantReviewId(UUID.randomUUID());
        review.setAuthor(user);
        review.setRestaurant(restaurant);
        review.setRating(request.getRating());
        review.setRatingGoodReason(request.getRatingGoodReason());
        review.setRatingBadReason(request.getRatingBadReason());
        review.setRatingOtherReason(request.getRatingOtherReason());

        var now = OffsetDateTime.now();
        review.setCreatedAt(now);
        review.setUpdatedAt(now);

        return restaurantReviewRepository.save(review);
    }

    @Override
    public RestaurantReview updateReview(UUID userId, UUID reviewId, UpdateRestaurantReviewRequest request) {
        Optional<RestaurantReview> optionalReview = restaurantReviewRepository.findById(reviewId);

        if (optionalReview.isEmpty()) {
            return null;
        }

        RestaurantReview review = optionalReview.get();

        // 로그인 유저와 작성자 일치 여부 확인
        if (!review.getAuthor().getUserId().equals(userId)) {
            return null;
        }

        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getRatingGoodReason() != null) {
            review.setRatingGoodReason(request.getRatingGoodReason());
        }
        if (request.getRatingBadReason() != null) {
            review.setRatingBadReason(request.getRatingBadReason());
        }
        if (request.getRatingOtherReason() != null) {
            review.setRatingOtherReason(request.getRatingOtherReason());
        }

        review.setUpdatedAt(OffsetDateTime.now());

        return restaurantReviewRepository.save(review);
    }

    @Override
    public void deleteReview(UUID userId, UUID reviewId) {
        Optional<RestaurantReview> optionalReview = restaurantReviewRepository.findById(reviewId);

        if (optionalReview.isEmpty()) {
            return; // 없는 리뷰이면 그냥 무시
        }

        RestaurantReview review = optionalReview.get();

        // 로그인 유저와 작성자 일치 여부 확인
        if (!review.getAuthor().getUserId().equals(userId)) {
            return;
        }

        // Soft delete
        review.setDeletedAt(OffsetDateTime.now());
        restaurantReviewRepository.save(review);
    }
}
