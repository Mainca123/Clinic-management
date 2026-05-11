package com.clinic.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data
@AllArgsConstructor
@Schema(description = "Information doctor register")
public class DoctorUpdateRequest {

    @Schema(example = "1")
    @NotNull(message = "user id not null")
    private Long userId;

    @Schema(example = "1")
    @NotNull(message = "department id not null")
    private Long departmentId;

    @Schema(example = "Chuyên môn")
    private String specialization;

    @Schema(example = "Năm kinh nghiệm")
    private Integer experienceYears;
}
