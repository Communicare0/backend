package com.example.backend.service;

import com.example.backend.JwtTokenProvider;
import com.example.backend.config.JwtConfig;
import com.example.backend.dto.request.EmailLoginRequest;
import com.example.backend.dto.request.EmailSignupRequest;
import com.example.backend.dto.response.EmailLoginResponse;
import com.example.backend.entity.RefreshToken;
import com.example.backend.entity.User;
import com.example.backend.entity.enums.AccountStatus;
import com.example.backend.repository.RefreshTokenRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtConfig jwtConfig;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public User signupWithEmail(EmailSignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        if (userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("Nickname already exists: " + request.getNickname());
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .status(AccountStatus.ACTIVE)
                .build();

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public EmailLoginResponse loginWithEmail(EmailLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Account is not active");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());

        OffsetDateTime refreshExpiresAt = OffsetDateTime.now().plusSeconds(jwtConfig.getRefreshTokenTtlMs() / 1000);

        RefreshToken refreshTokenEntity = refreshTokenRepository.findByUserId(user.getUserId())
                .map(rt -> rt.updateToken(refreshToken, refreshExpiresAt))
                .orElse(RefreshToken.builder()
                        .userId(user.getUserId())
                        .refreshToken(refreshToken)
                        .expiresAt(refreshExpiresAt)
                        .build());

        refreshTokenRepository.save(refreshTokenEntity);

        return EmailLoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getAccessTokenTtlMs() / 1000)
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }
}
