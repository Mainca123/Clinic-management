package com.clinic.domain.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SearchResponse {

    private List<DoctorResponse> doctors;

    private List<UserResponse> patients;

    private List<DepartmentResponse> departments;

    private List<AppointmentResponse> appointments;
}