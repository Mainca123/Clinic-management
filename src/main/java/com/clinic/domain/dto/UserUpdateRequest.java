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
@Schema(description = "Information user")
public class UserUpdateRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name must not exceed 150 characters")
    @Schema(example = "Nguyễn Chí Minh")
    private String fullName;

    @Size(max = 10, message = "Phone number must not exceed 20 characters")
    @Pattern(regexp = "^[0-9+()-]*$", message = "Phone number contains invalid characters")
    @Schema(example = "0990012345")
    private String phone;

    @NotNull(message = "Gender is required")
    @Schema(example = "OTHER")
    private Gender gender;

    @Schema(example = "2006-12-21")
    private LocalDate dateOfBirth;

}
