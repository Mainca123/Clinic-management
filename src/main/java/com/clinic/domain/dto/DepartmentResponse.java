package com.clinic.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartmentResponse {
    private long id;
    private String name;
    private String description;
}
