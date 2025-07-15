package org.minh.template.service.auth;

import org.minh.template.entity.JwtToken;

import java.util.UUID;

public interface JwtTokenService {
    JwtToken findByUserIdAndRefreshToken(UUID id, String refreshToken);

    JwtToken findByTokenOrRefreshToken(String token);

    void save(JwtToken jwtToken);

    void delete(JwtToken jwtToken);
}