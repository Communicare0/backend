package com.example.backend.controller;

import com.example.backend.dto.request.CreateRestaurantRequest;
import com.example.backend.dto.request.UpdateRestaurantRequest;
import com.example.backend.dto.response.RestaurantResponse;
import com.example.backend.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/restaurants")
@Tag(name = "Restaurant", description = "Restaurant API")
public class RestaurantController {
    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
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

    @GetMapping
    @Operation(
        summary = "Get all restaurants",
        description = "Get all visible restaurants",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Restaurants found successfully",
                content = @Content(schema = @Schema(implementation = RestaurantResponse.class))
            )
        }
    )
    public ResponseEntity<List<RestaurantResponse>> getAllRestaurants() {
        List<RestaurantResponse> restaurants = restaurantService.getAllRestaurants();
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/recommended")
    @Operation(
        summary = "Get recommended restaurants for current user",
        description = "Get restaurants prioritized by user's preferred food type and sorted by average rating",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Recommended restaurants found successfully",
                content = @Content(schema = @Schema(implementation = RestaurantResponse.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "User not found"
            )
        }
    )
    public ResponseEntity<List<RestaurantResponse>> getRecommendedRestaurants() {
        try {
            UUID userId = getCurrentUserId();
            List<RestaurantResponse> restaurants = restaurantService.getRecommendedRestaurants(userId);
            return ResponseEntity.ok(restaurants);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{restaurantId}")
    @Operation(
        summary = "Get restaurant by ID",
        description = "Get a specific restaurant by its ID",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Restaurant found successfully",
                content = @Content(schema = @Schema(implementation = RestaurantResponse.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Restaurant not found"
            )
        }
    )
    public ResponseEntity<RestaurantResponse> getRestaurantById(
            @Parameter(description = "Restaurant ID", required = true)
            @PathVariable UUID restaurantId) {
        try {
            RestaurantResponse restaurant = restaurantService.getRestaurantById(restaurantId);
            return ResponseEntity.ok(restaurant);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping
    @Operation(
        summary = "Create a new restaurant",
        description = "Create a new restaurant with the provided information",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Restaurant created successfully",
                content = @Content(schema = @Schema(implementation = RestaurantResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid request body"
            )
        }
    )
    public ResponseEntity<RestaurantResponse> createRestaurant(
            @Parameter(description = "Restaurant creation request", required = true)
            @Valid @RequestBody CreateRestaurantRequest request) {
        RestaurantResponse restaurant = restaurantService.createRestaurant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurant);
    }

    @PutMapping("/{restaurantId}")
    @Operation(
        summary = "Update a restaurant",
        description = "Update an existing restaurant with the provided information",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Restaurant updated successfully",
                content = @Content(schema = @Schema(implementation = RestaurantResponse.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Restaurant not found"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid request body"
            )
        }
    )
    public ResponseEntity<RestaurantResponse> updateRestaurant(
            @Parameter(description = "Restaurant ID", required = true)
            @PathVariable UUID restaurantId,
            @Parameter(description = "Restaurant update request", required = true)
            @Valid @RequestBody UpdateRestaurantRequest request) {
        try {
            RestaurantResponse restaurant = restaurantService.updateRestaurant(restaurantId, request);
            return ResponseEntity.ok(restaurant);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{restaurantId}")
    @Operation(
        summary = "Delete a restaurant",
        description = "Soft delete a restaurant (mark as deleted)",
        responses = {
            @ApiResponse(
                responseCode = "204",
                description = "Restaurant deleted successfully"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Restaurant not found"
            )
        }
    )
    public ResponseEntity<Void> deleteRestaurant(
            @Parameter(description = "Restaurant ID", required = true)
            @PathVariable UUID restaurantId) {
        try {
            restaurantService.deleteRestaurant(restaurantId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
