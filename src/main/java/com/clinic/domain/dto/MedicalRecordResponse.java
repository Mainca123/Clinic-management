package com.clinic.domain.dto;

import lombok.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordResponse {
    private Long id;
    private Long appointmentId;
    private String diagnosis;
    private String treatmentPlan;
    private LocalDate reexaminationDate;
    private Instant createdAt;
    private List<PrescriptionDetailResponse> prescriptionDetails;
}