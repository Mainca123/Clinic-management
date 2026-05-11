package com.clinic.domain.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "Information department register")
public class DepartmentRequest {

    @NotBlank(message = "department name not null")
    private String name;

    private String description;
}
