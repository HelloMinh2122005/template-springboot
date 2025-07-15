package org.minh.template.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class RegisterRequest {
    @NotBlank(message = "{not_blank}")
    @Email(message = "{invalid_email}")
    @Size(max = 100, message = "{max_length}")
    @Schema(
            name = "email",
            description = "Email of the user",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "mail@example.com"
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

    @NotBlank(message = "{not_blank}")
    @Schema(
            name = "passwordConfirm",
            description = "Password for confirmation",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "P@sswd123."
    )
    private String passwordConfirm;

    @NotBlank(message = "{not_blank}")
    @Size(max = 50, message = "{max_length}")
    @Schema(
            name = "name",
            description = "Name of the user",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "John"
    )
    private String name;

    @NotBlank(message = "{not_blank}")
    @Size(max = 50, message = "{max_length}")
    @Schema(
            name = "lastName",
            description = "Lastname of the user",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "DOE"
    )
    private String lastName;

    @Size(max = 20, message = "{max_length}")
    @Schema(
            name = "phoneNumber",
            description = "Phone number of the user",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "+123456789"
    )
    private String phoneNumber;

    @Size(max = 10, message = "{max_length}")
    @Schema(
            name = "gender",
            description = "Gender of the user",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "Male"
    )
    private String gender;

    @Schema(
            name = "birthDate",
            description = "Birth date of the user",
            type = "string",
            format = "date-time",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "2000-01-01T00:00:00"
    )
    private LocalDateTime birthDate;

}