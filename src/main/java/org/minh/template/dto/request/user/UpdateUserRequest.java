package org.minh.template.dto.request.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class UpdateUserRequest {
    private String name;
    private String lastName;
    private String phoneNumber;
    private String gender;
    private LocalDateTime birthDate;
}
