package com.clinic.domain.dto;

import com.clinic.constant.Gender;
import com.clinic.constant.RoleType;
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
    private Long id;
    private String username;
    private String fullName;
    private String phone;
    private String email;
    private RoleType role;
    private Gender gender;
    private LocalDate dateOfBirth;
    private Boolean isVerified;
    private String avatarUrl;
    private Boolean isDeleted;
}
