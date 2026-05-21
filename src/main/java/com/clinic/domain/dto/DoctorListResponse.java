package com.clinic.domain.dto;

import com.clinic.domain.entity.Doctor;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DoctorListResponse {
    private List<DoctorResponse> doctorList;
    private long totalItems;
    private int totalPages;
}
