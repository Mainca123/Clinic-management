package com.clinic.ai.dto;

import com.clinic.domain.dto.MedicalRecordResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientPayload {
    private Long patientId;
    private List<MedicalRecordResponse> records;
    private String summary;
    private LocalDate reexaminationDate;
}
