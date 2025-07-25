package org.minh.template.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "{not_blank}")
    @Email(message = "{invalid_email}")
    @Schema(
            name = "email",
            description = "E-mail of the user",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "admin@example.com"
    )
    private String email;

    @NotBlank(message = "{not_blank}")
    @Schema(
            name = "password",
            description = "Password of the user",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "P@sswd123."
    )
    private String password;
}