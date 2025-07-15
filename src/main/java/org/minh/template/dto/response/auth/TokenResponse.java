package org.minh.template.dto.response.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class TokenResponse {
    @Schema(
            name = "token",
            description = "Token",
            type = "String",
            example = "eyJhbGciOiJIUzUxMiJ9..."
    )
    private String accessToken;

    @Schema(
            name = "refreshToken",
            description = "Refresh Token",
            type = "String",
            example = "eyJhbGciOiJIUzUxMiJ9..."
    )
    private String refreshToken;

    private String id;
    private String role;
}
