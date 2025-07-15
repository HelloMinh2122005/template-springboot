package org.minh.template.service.auth;

import org.minh.template.dto.response.auth.TokenResponse;
import org.minh.template.entity.User;

import java.util.UUID;

public interface AuthService {
    TokenResponse login(String email, String password, Boolean rememberMe);

    TokenResponse refreshFromBearerString(String bearer);

    void logout(User user, String bearer);

    void logout(User user);

    TokenResponse refresh(String refreshToken);

    TokenResponse generateTokens(UUID id, Boolean rememberMe);
}