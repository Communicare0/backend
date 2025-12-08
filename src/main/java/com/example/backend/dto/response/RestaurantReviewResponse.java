package com.example.backend.dto.response;

import com.example.backend.entity.RestaurantReview;
import com.example.backend.entity.enums.Nationality;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantReviewResponse {
    private UUID restaurantReviewId;
    private UUID restaurantId;

    private String authorDepartment;   // 작성자 학과
    private String authorStudentYear;  // 예: "21학번"
    private Nationality authorNationality; // 작성자 국적

    private UUID authorId;
    private Integer rating;
    private String reason;
    private OffsetDateTime createdAt;
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

    public static RestaurantReviewResponse fromEntity(RestaurantReview restaurantReview) {
        RestaurantReviewResponse response = new RestaurantReviewResponse();
        response.setRestaurantReviewId(restaurantReview.getRestaurantReviewId());
        response.setRestaurantId(restaurantReview.getRestaurant().getRestaurantId());

        response.setAuthorDepartment(restaurantReview.getAuthor().getDepartment());
        response.setAuthorStudentYear(maskStudentIdToYear(restaurantReview.getAuthor().getStudentId()));
        response.setAuthorNationality(restaurantReview.getAuthor().getNationality());


        response.setAuthorId(restaurantReview.getAuthor().getUserId());
        response.setRating(restaurantReview.getRating());
        response.setReason(restaurantReview.getReason());
        response.setCreatedAt(restaurantReview.getCreatedAt());
        response.setUpdatedAt(restaurantReview.getUpdatedAt());

        return response;
    }
}
