package com.example.backend.controller;

import com.example.backend.dto.request.CreateRestaurantReviewRequest;
import com.example.backend.dto.request.UpdateRestaurantRequest;
import com.example.backend.dto.request.UpdateRestaurantReviewRequest;
import com.example.backend.dto.response.RestaurantResponse;
import com.example.backend.dto.response.RestaurantReviewListResponse;
import com.example.backend.dto.response.RestaurantReviewResponse;
import com.example.backend.entity.Restaurant;
import com.example.backend.entity.RestaurantReview;
import com.example.backend.service.RestaurantReviewService;
import com.example.backend.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/restaurantReviews")
@Tag(name = "RestaurantReview", description = "RestaurantReview API")
public class RestaurantReviewController {
    private final RestaurantReviewService restaurantReviewService;
    private final RestaurantService restaurantService;

    public RestaurantReviewController(
        RestaurantReviewService restaurantReviewService,
        RestaurantService restaurantService
    ) {
        this.restaurantReviewService = restaurantReviewService;
        this.restaurantService = restaurantService;
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication != null ? authentication.getPrincipal() : null;

        if (principal instanceof UUID uuid) {
            return uuid;
        }

        throw new IllegalStateException("인증 정보에서 사용자 ID를 찾을 수 없습니다.");
    }



    @GetMapping("/restaurant/{restaurantId}")
    @Operation(
        summary = "해당 레스토랑의 리뷰 목록 조회",
        parameters = {
            @Parameter(
                name = "restaurantId",
                description = "리뷰를 조회할 레스토랑의 UUID",
                required = true
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "리뷰 목록 조회 성공",
                content = @Content(schema = @Schema(implementation = RestaurantReviewResponse.class))
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
        }
    )
    public ResponseEntity<List<RestaurantReviewResponse>> getReviewsByRestaurant(@PathVariable UUID restaurantId) {
        List<RestaurantReview> reviews = restaurantReviewService.findReviewsByRestaurantId(restaurantId);

        List<RestaurantReviewResponse> response =
            reviews.stream()
                    .map(RestaurantReviewResponse::fromEntity)
                    .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    @Operation(
        summary = "현재 로그인한 사용자의 리뷰 목록 조회",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "사용자 리뷰 목록 조회 성공",
                content = @Content(schema = @Schema(implementation = RestaurantReviewResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
        }
    )
    public ResponseEntity<List<RestaurantReviewResponse>> getUserReviews() {
        try {
            UUID userId = getCurrentUserId();
            List<RestaurantReview> reviews = restaurantReviewService.findReviewsByUserId(userId);

            List<RestaurantReviewResponse> response =
                reviews.stream()
                        .map(RestaurantReviewResponse::fromEntity)
                        .toList();

            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{reviewId}")
    @Operation(
        summary = "특정 리뷰 조회",
        parameters = {
            @Parameter(
                name = "reviewId",
                description = "조회할 리뷰의 UUID",
                required = true
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "리뷰 조회 성공",
                content = @Content(schema = @Schema(implementation = RestaurantReviewResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
        }
    )
    public ResponseEntity<RestaurantReviewResponse> getReviewById(@PathVariable UUID reviewId) {
        RestaurantReview review = restaurantReviewService.getReviewById(reviewId);

        if (review == null) {
            return ResponseEntity.notFound().build();
        }

        RestaurantReviewResponse response = RestaurantReviewResponse.fromEntity(review);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(
        summary = "리뷰 생성",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "restaurantId, rating, reason",
            required = true,
            content = @Content(schema = @Schema(implementation = CreateRestaurantReviewRequest.class))
        ),
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "리뷰 생성 성공",
                content = @Content(schema = @Schema(implementation = RestaurantReviewResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
        }
    )
    public ResponseEntity<RestaurantReviewResponse> createReview(
            @RequestBody CreateRestaurantReviewRequest request
    ) {
        try {
            if (request.getRestaurantId() == null || request.getRating() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            // 평점 유효성 검사 (1-5)
            if (request.getRating() < 1 || request.getRating() > 5) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            UUID userId = getCurrentUserId();

            RestaurantReview created = restaurantReviewService.createReview(userId, request);
            RestaurantReviewResponse response = RestaurantReviewResponse.fromEntity(created);

            RestaurantResponse restaurant = restaurantService.getRestaurantById(request.getRestaurantId());
            UpdateRestaurantRequest updateRestaurantRequest = getUpdateRestaurantRequest(1, restaurant, response);

            restaurantService.updateRestaurant(restaurant.getRestaurantId(), updateRestaurantRequest);


            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{reviewId}")
    @Operation(
        summary = "리뷰 수정",
        parameters = {
            @Parameter(
                name = "reviewId",
                description = "수정할 리뷰의 UUID",
                required = true
            )
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(schema = @Schema(implementation = UpdateRestaurantReviewRequest.class))
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "리뷰 수정 성공",
                content = @Content(schema = @Schema(implementation = RestaurantReviewResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 권한 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
        }
    )
    public ResponseEntity<RestaurantReviewResponse> updateReview(
            @PathVariable UUID reviewId,
            @RequestBody UpdateRestaurantReviewRequest request
    ) {
        try {
            if (reviewId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            // 평점 유효성 검사 (1-5)
            if (request.getRating() != null && (request.getRating() < 1 || request.getRating() > 5)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            UUID userId = getCurrentUserId();

            RestaurantReview updated = restaurantReviewService.updateReview(userId, reviewId, request);
            if (updated == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            RestaurantReviewResponse response = RestaurantReviewResponse.fromEntity(updated);

            RestaurantResponse restaurant = restaurantService.getRestaurantById(updated.getRestaurant().getRestaurantId());
            UpdateRestaurantRequest updateRestaurantRequest = getUpdateRestaurantRequest(0, restaurant, response);
            restaurantService.updateRestaurant(restaurant.getRestaurantId(), updateRestaurantRequest);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private static UpdateRestaurantRequest getUpdateRestaurantRequest(Integer addNum, RestaurantResponse restaurant, RestaurantReviewResponse response) {
        UpdateRestaurantRequest updateRestaurantRequest = new UpdateRestaurantRequest();
        updateRestaurantRequest.setRatingCount(restaurant.getRatingCount() + addNum);
        updateRestaurantRequest.setRatingSum(restaurant.getRatingSum() + response.getRating());
        updateRestaurantRequest.setAvgRating(
            (
                ((restaurant.getAvgRating()
                    .multiply(BigDecimal.valueOf(restaurant.getRatingCount() - 1 + addNum)))
                    .add(BigDecimal.valueOf(response.getRating())))
                        .divide(BigDecimal.valueOf(restaurant.getRatingCount() + addNum), 3, RoundingMode.HALF_EVEN)
            )
        );  // 뭔가 복잡해보이지만 big decimal을 쓰려면 어쩔 수가 없었어요 ㅠㅠ
        return updateRestaurantRequest;
    }

    @DeleteMapping("/{reviewId}")
    @Operation(
        summary = "리뷰 삭제",
        parameters = {
            @Parameter(
                name = "reviewId",
                description = "삭제할 리뷰의 UUID",
                required = true
            )
        },
        responses = {
            @ApiResponse(responseCode = "204", description = "리뷰 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "권한 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
        }
    )
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID reviewId
    ) {
        try {
            if (reviewId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            UUID userId = getCurrentUserId();

            Integer deleteRating = restaurantReviewService.getReviewById(reviewId).getRating();
            UUID restaurantId = restaurantReviewService.getReviewById(reviewId).getRestaurant().getRestaurantId();
            restaurantReviewService.deleteReview(userId, reviewId);

            RestaurantResponse restaurant = restaurantService.getRestaurantById(restaurantId);

            UpdateRestaurantRequest updateRestaurantRequest = new UpdateRestaurantRequest();
            if (restaurant.getRatingCount() - 1 > 0) {
                updateRestaurantRequest.setRatingCount(restaurant.getRatingCount() - 1);
                updateRestaurantRequest.setRatingSum(restaurant.getRatingSum() - deleteRating);
                updateRestaurantRequest.setAvgRating(
                    BigDecimal.valueOf(updateRestaurantRequest.getRatingSum())
                        .divide(BigDecimal.valueOf(updateRestaurantRequest.getRatingCount()), 3, RoundingMode.HALF_EVEN)
                );
            } else {
                updateRestaurantRequest.setRatingCount(0);
                updateRestaurantRequest.setRatingSum(0);
                updateRestaurantRequest.setAvgRating(BigDecimal.valueOf(0));
            }

            restaurantService.updateRestaurant(restaurantId, updateRestaurantRequest);

            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/restaurant/{restaurantId}/summary")
    @Operation(
        summary = "레스토랑 리뷰 요약 정보 조회",
        parameters = {
            @Parameter(
                name = "restaurantId",
                description = "리뷰 요약을 조회할 레스토랑의 UUID",
                required = true
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "리뷰 요약 정보 조회 성공",
                content = @Content(schema = @Schema(implementation = RestaurantReviewListResponse.class))
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
        }
    )
    public ResponseEntity<RestaurantReviewListResponse> getRestaurantReviewSummary(@PathVariable UUID restaurantId) {
        List<RestaurantReview> reviews = restaurantReviewService.findReviewsByRestaurantId(restaurantId);

        List<RestaurantReviewResponse> reviewResponses =
            reviews.stream()
                    .map(RestaurantReviewResponse::fromEntity)
                    .toList();

        RestaurantReviewListResponse response = new RestaurantReviewListResponse(
            restaurantId,
            reviewResponses.size(),
            reviewResponses
        );

        return ResponseEntity.ok(response);
    }
}
