package com.example.backend.service;

import com.example.backend.dto.request.EmailLoginRequest;
import com.example.backend.dto.request.EmailSignupRequest;
import com.example.backend.dto.response.EmailLoginResponse;
import com.example.backend.entity.User;

public interface UserService {
    User signupWithEmail(EmailSignupRequest request);
    EmailLoginResponse loginWithEmail(EmailLoginRequest request);
}
