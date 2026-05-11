package com.clinic.domain.dto;

import com.clinic.constant.Gender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;
@Getter
@Setter
@AllArgsConstructor
@Schema(description = "Information doctor register")
public class DoctorCreateRequest {

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
    @Schema(example = "giangchinhtgp@gmail.com")
    private String email;

    @NotNull(message = "Gender is required")
    @Schema(example = "OTHER")
    private Gender gender;

    @Schema(example = "2006-12-21")
    private LocalDate dateOfBirth;

    @Schema(example = "1")
    @NotNull(message = "departmentID not null")
    private Long departmentId;

    @Schema(example = "Chuyên môn")
    private String specialization;

    @Schema(example = "3")
    private Integer experienceYears;
}
