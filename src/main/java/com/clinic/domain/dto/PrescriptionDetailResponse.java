package com.clinic.domain.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionDetailResponse {
    private Long id;
    private Long medicineId;
    private String medicineName; // Trả thêm tên thuốc để Frontend đỡ phải tự map
    private String unit;         // Trả thêm đơn vị thuốc (viên/vỉ...)
    private Integer quantity;
    private String dosage;
}