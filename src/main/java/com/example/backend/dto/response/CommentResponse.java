package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.example.backend.entity.Comment;
import com.example.backend.entity.enums.Nationality;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private UUID commentId;
    private UUID postId;
    private String content;

    private String authorDepartment;       
    private String authorStudentYear;      
    private Nationality authorNationality;

    private static String maskStudentIdToYear(String studentId) {
        if (studentId == null) return null;

        if (studentId.length() >= 4) {
            try {
                String yearStr = studentId.substring(0, 4);   // "2021"
                int year = Integer.parseInt(yearStr);
                int shortYear = year % 100;                  // 21
                return shortYear + "학번";
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

    public static CommentResponse fromEntity(Comment comment) {
        
        CommentResponse response = new CommentResponse();

        var author = comment.getAuthor();

        response.setCommentId(comment.getCommentId());
        response.setPostId(comment.getPost().getPostId());
        response.setContent(comment.getContent());

        if (author != null) {
            response.setAuthorDepartment(author.getDepartment());
            response.setAuthorStudentYear(maskStudentIdToYear(author.getStudentId()));
            response.setAuthorNationality(author.getNationality());
        }

        return response;
    }
}