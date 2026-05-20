package com.clinic.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DepartmentListResponse {
    private List<DepartmentResponse> departmentResponseList;
    private long totalItems;
    private int totalPages;
}
