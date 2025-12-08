package com.example.backend.service;

import com.example.backend.dto.request.CreateRestaurantRequest;
import com.example.backend.dto.request.UpdateRestaurantRequest;
import com.example.backend.dto.response.RestaurantResponse;
import com.example.backend.entity.Restaurant;
import com.example.backend.entity.User;
import com.example.backend.entity.enums.Nationality;
import com.example.backend.entity.enums.PreferredFoodType;
import com.example.backend.entity.enums.RestaurantStatus;
import com.example.backend.entity.enums.RestaurantType;
import com.example.backend.repository.RestaurantRepository;
import com.example.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestaurantServiceImpl implements RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    @Override
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .filter(restaurant -> restaurant.getStatus() == RestaurantStatus.VISIBLE)
                .map(RestaurantResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public RestaurantResponse getRestaurantById(UUID restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .filter(r -> r.getStatus() == RestaurantStatus.VISIBLE)
                .orElseThrow(() -> new EntityNotFoundException("레스토랑을 찾을 수 없습니다: " + restaurantId));

        return RestaurantResponse.fromEntity(restaurant);
    }

    @Override
    @Transactional
    public RestaurantResponse createRestaurant(CreateRestaurantRequest request) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.getName());
        restaurant.setGoogleMapUrl(request.getGoogleMapUrl());
        restaurant.setRestaurantType(request.getRestaurantType());
        restaurant.setStatus(RestaurantStatus.VISIBLE);
        restaurant.setRatingCount(0);
        restaurant.setRatingSum(0);
        restaurant.setAvgRating(BigDecimal.valueOf(0));

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return RestaurantResponse.fromEntity(savedRestaurant);
    }

    @Override
    @Transactional
    public RestaurantResponse updateRestaurant(UUID restaurantId, UpdateRestaurantRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .filter(r -> r.getStatus() == RestaurantStatus.VISIBLE)
                .orElseThrow(() -> new EntityNotFoundException("레스토랑을 찾을 수 없습니다: " + restaurantId));

        restaurant.setName(request.getName());
        restaurant.setGoogleMapUrl(request.getGoogleMapUrl());
        if (request.getRestaurantType() != null) {
            restaurant.setRestaurantType(request.getRestaurantType());
        }

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        return RestaurantResponse.fromEntity(updatedRestaurant);
    }

    @Override
    @Transactional
    public void deleteRestaurant(UUID restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .filter(r -> r.getStatus() == RestaurantStatus.VISIBLE)
                .orElseThrow(() -> new EntityNotFoundException("레스토랑을 찾을 수 없습니다: " + restaurantId));

        restaurant.setStatus(RestaurantStatus.DELETED);
        restaurant.setDeletedAt(java.time.OffsetDateTime.now());
        restaurantRepository.save(restaurant);
    }

    @Override
    public List<RestaurantResponse> getRecommendedRestaurants(UUID userId) {
        // 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        Map<Nationality, RestaurantType> nationalityToType = new HashMap<>();

        nationalityToType.put(Nationality.KOREAN, RestaurantType.KOREA);
        nationalityToType.put(Nationality.CHINESE, RestaurantType.CHINA);
        nationalityToType.put(Nationality.JAPANESE, RestaurantType.JAPAN);
        nationalityToType.put(Nationality.VIETNAMESE, RestaurantType.VIETNAM);

        RestaurantType preferredRestaurantType = RestaurantType.NONE;
        // 사용자의 선호 음식 타입을 RestaurantType으로 변환
        if (user.getPreferredFoodType() != PreferredFoodType.NONE) {
            preferredRestaurantType = convertToRestaurantType(user.getPreferredFoodType());
        } else if (
            user.getNationality() == Nationality.KOREAN || user.getNationality() == Nationality.CHINESE || user.getNationality() == Nationality.JAPANESE || user.getNationality() == Nationality.VIETNAMESE
        ) {
            preferredRestaurantType = nationalityToType.get(user.getNationality());
        }

        // 추천 순서로 레스토랑 조회
        List<Restaurant> restaurants = restaurantRepository.findByRecommendedOrder(
                RestaurantStatus.VISIBLE,
                preferredRestaurantType
        );

        return restaurants.stream()
                .map(RestaurantResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * PreferredFoodType을 RestaurantType으로 변환
     */
    private RestaurantType convertToRestaurantType(PreferredFoodType preferredFoodType) {
        if (preferredFoodType == null) {
            return RestaurantType.NONE;
        }

        switch (preferredFoodType) {
            case HALAL:
                return RestaurantType.HALAL;
            case KOSHER:
                return RestaurantType.KOSHER;
            case VEGAN:
                return RestaurantType.VEGAN;
            case NONE:
            default:
                return RestaurantType.NONE;
        }
    }
}
