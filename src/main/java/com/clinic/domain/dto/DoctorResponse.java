package com.clinic.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorResponse {

    private Long id;

    private String fullName;

    private String email;

    private String departmentName;

    private String specialization;

    private Integer experienceYears;
}
