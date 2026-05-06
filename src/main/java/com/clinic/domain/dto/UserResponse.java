package com.clinic.domain.dto;

import com.clinic.constant.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "Information user")
@Builder
public class UserResponse {
    private String fullName;
    private String phone;
    private String email;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String avatarUrl;
}
