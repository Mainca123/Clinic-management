package com.clinic.domain.dto;

import com.clinic.constant.Gender;
import com.clinic.constant.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "Information register")
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters")
    @Schema(example = "username")
    private String username;

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

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name must not exceed 150 characters")
    @Schema(example = "Nguyễn Chí Minh")
    private String fullName;

    @Size(max = 10, message = "Phone number must not exceed 20 characters")
    @Pattern(regexp = "^[0-9+()-]*$", message = "Phone number contains invalid characters")
    @Schema(example = "0990012345")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Schema(example = "username@gmail.com")
    private String email;

    @NotNull(message = "Gender is required")
    @Schema(example = "OTHER")
    private Gender gender;

    @Schema(example = "2006-12-21")
    private LocalDate dateOfBirth;

    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    @Pattern(regexp = "^(https?://.*)?$", message = "Avatar URL must be a valid URL or empty")
    @Schema(example = "https://www.google.com")
    private String avatarUrl;
}