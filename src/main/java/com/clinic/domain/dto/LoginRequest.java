package com.clinic.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jdk.jfr.Description;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "Information login")
public class LoginRequest {

    @NotBlank(message = "USERNAME CANNOT BE LEFT BLANK")
    @Schema(example = "admin")
    private String username;

    @NotBlank(message = "PASSWORD CANNOT BE LEFT BLANK")
    @Schema(example = "abcd123")
    private String password;
}
