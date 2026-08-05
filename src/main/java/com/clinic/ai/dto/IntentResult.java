package com.clinic.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IntentResult {

    private String intent;

    private Map<String, Object> params;

    private Long departmentId;

    private String urgency;

    private Double confidence;
}
