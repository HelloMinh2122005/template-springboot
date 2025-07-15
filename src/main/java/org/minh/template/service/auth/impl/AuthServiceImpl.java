package org.minh.template.service.auth.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.minh.template.dto.response.auth.TokenResponse;
import org.minh.template.entity.JwtToken;
import org.minh.template.entity.User;
import org.minh.template.exception.NotFoundException;
import org.minh.template.exception.RefreshTokenExpiredException;
import org.minh.template.security.JwtTokenProvider;
import org.minh.template.security.JwtUserDetails;
import org.minh.template.service.MessageSourceService;
import org.minh.template.service.auth.AuthService;
import org.minh.template.service.auth.JwtTokenService;
import org.minh.template.service.user.UserService;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.minh.template.util.Constants.TOKEN_HEADER;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserService userService;

    private final JwtTokenService jwtTokenService;

    private final AuthenticationManager authenticationManager;

    private final JwtTokenProvider jwtTokenProvider;

    private final HttpServletRequest httpServletRequest;

    private final MessageSourceService messageSourceService;

    /**
     * Authenticate user.
     *
     * @param email      String
     * @param password   String
     * @param rememberMe Boolean
     * @return TokenResponse
     */
    @Override
    @Transactional
    public TokenResponse login(String email, final String password, final Boolean rememberMe) {
        log.info("Login request received: {}", email);

        String badCredentialsMessage = messageSourceService.get("Unauthorized");

        User user = null;
        try {
            user = userService.findByEmail(email);
            email = user.getEmail();
        } catch (NotFoundException e) {
            log.error("User not found with email: {}", email);
            throw new AuthenticationCredentialsNotFoundException(badCredentialsMessage);
        }

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, password);
        try {
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            JwtUserDetails jwtUserDetails = jwtTokenProvider.getPrincipal(authentication);

            return generateTokens(UUID.fromString(jwtUserDetails.getId()), rememberMe);
        } catch (NotFoundException e) {
            log.error("Authentication failed for email: {}", email);
            throw new AuthenticationCredentialsNotFoundException(badCredentialsMessage);
        }
    }

    /**
     * Refresh from bearer string.
     *
     * @param bearer String
     * @return TokenResponse
     */
    @Override
    @Transactional
    public TokenResponse refreshFromBearerString(final String bearer) {
        return refresh(jwtTokenProvider.extractJwtFromBearerString(bearer));
    }

    /**
     * Logout from bearer string by user.
     *
     * @param user   User
     * @param bearer String
     */
    @Override
    @Transactional
    public void logout(User user, final String bearer) {
        JwtToken jwtToken = jwtTokenService.findByTokenOrRefreshToken(
                jwtTokenProvider.extractJwtFromBearerString(bearer));

        if (!user.getId().equals(jwtToken.getUserId())) {
            log.error("User id: {} is not equal to token user id: {}", user.getId(), jwtToken.getUserId());
            throw new AuthenticationCredentialsNotFoundException(messageSourceService.get("bad_credentials"));
        }

        jwtTokenService.delete(jwtToken);
    }

    /**
     * Logout from bearer string by user.
     *
     * @param user User
     */
    @Override
    @Transactional
    public void logout(User user) {
        logout(user, httpServletRequest.getHeader(TOKEN_HEADER));
    }

    /**
     * Refresh token.
     *
     * @param refreshToken String
     * @return TokenResponse
     */
    @Override
    @Transactional
    public TokenResponse refresh(final String refreshToken) {
        log.info("Refresh request received: {}", refreshToken);

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.error("Refresh token is expired.");
            throw new RefreshTokenExpiredException();
        }

        User user = jwtTokenProvider.getUserFromToken(refreshToken);
        JwtToken oldToken = jwtTokenService.findByUserIdAndRefreshToken(user.getId(), refreshToken);
        if (oldToken != null && oldToken.getRememberMe()) {
            jwtTokenProvider.setRememberMe();
        }

        boolean rememberMe = false;
        if (oldToken != null) {
            rememberMe = oldToken.getRememberMe();
            jwtTokenService.delete(oldToken);
        }

        return generateTokens(user.getId(), rememberMe);
    }

    /**
     * Generate both access and refresh tokens.
     *
     * @param id         user identifier to set the subject for token and value for the expiring map
     * @param rememberMe Boolean option to set the expiration time for refresh token
     * @return an object of TokenResponse
     */
    @Override
    @Transactional
    public TokenResponse generateTokens(final UUID id, final Boolean rememberMe) {
        String token = jwtTokenProvider.generateJwt(id.toString());
        String refreshToken = jwtTokenProvider.generateRefresh(id.toString());
        if (rememberMe) {
            jwtTokenProvider.setRememberMe();
        }

        jwtTokenService.save(JwtToken.builder()
                .userId(id)
                .token(token)
                .refreshToken(refreshToken)
                .rememberMe(rememberMe)
                .ipAddress(httpServletRequest.getRemoteAddr())
                .userAgent(httpServletRequest.getHeader("User-Agent"))
                .tokenTimeToLive(jwtTokenProvider.getRefreshTokenExpiresIn())
                .build());
        log.info("Token generated for user: {}", id);

        User user = userService.findById(id);
        String role = user.getRole().getName().getValue();

        return TokenResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .id(String.valueOf(id))
                .role(role)
                .build();
    }
}