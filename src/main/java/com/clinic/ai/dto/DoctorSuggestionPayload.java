package com.clinic.ai.dto;

import com.clinic.domain.dto.DoctorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSuggestionPayload {

    private Long departmentId;

    // Chuyên khoa AI đề xuất
    private String department;

    // Mức độ khẩn cấp (LOW, MEDIUM, HIGH)
    private String urgency;

    // Lời khuyên ban đầu
    private String advice;

    private Double confidence;

    // Danh sách bác sĩ
    private List<DoctorResponse> doctors;
}
