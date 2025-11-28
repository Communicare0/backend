package com.example.backend.dto.response;

import com.example.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailSignupResponse {
    private UUID userId;
    private String email;
    private String nickname;

    public static EmailSignupResponse fromEntity(User user) {
        EmailSignupResponse response = new EmailSignupResponse();
        response.setUserId(user.getUserId());
        response.setEmail(user.getEmail());
        response.setNickname(user.getNickname());
        return response;
    }
}
