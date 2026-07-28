package com.clinic.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OllamaResponse {

    private String model;

    @JsonProperty("created_at")
    private String createdAt;

    private Message message;

    private boolean done;

}