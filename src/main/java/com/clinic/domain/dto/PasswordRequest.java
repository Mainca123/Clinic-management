package com.clinic.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "Information change password")
public class PasswordRequest {
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!*_?.-]).*$",
            message = "Password must satisfy at least 3 out of 4 following conditions: " +
                    "1. At least one lowercase letter (a-z), " +
                    "2. At least one uppercase letter (A-Z), " +
                    "3. At least one digit (0-9), " +
                    "4. At least one special character (@#$%^&+=!*_?.-)")
    @Schema(example = "Abc123")
    private String password;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!*_?.-]).*$",
            message = "Password must satisfy at least 3 out of 4 following conditions: " +
                    "1. At least one lowercase letter (a-z), " +
                    "2. At least one uppercase letter (A-Z), " +
                    "3. At least one digit (0-9), " +
                    "4. At least one special character (@#$%^&+=!*_?.-)")
    @Schema(example = "Abc123")
    private String confirmPassword;
}
