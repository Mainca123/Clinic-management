package com.clinic.ai.dto;

import com.clinic.domain.dto.PrescriptionDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicinePayload {
    private Long medicalRecordId;
    private List<PrescriptionDetailResponse> medicines;
    private String explanation;
    private String warning;
}
