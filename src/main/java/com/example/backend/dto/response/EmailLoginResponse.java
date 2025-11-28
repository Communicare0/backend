package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailLoginResponse {
    // 토큰 관련
    private String accessToken;
    private String refreshToken;    // 선택
    private String tokenType;       // "Bearer"
    private long expiresIn;         // access token 만료 시간(초 단위 등)

    // 유저 요약 정보
    private UUID userId;
    private String email;
    private String nickname;
}
