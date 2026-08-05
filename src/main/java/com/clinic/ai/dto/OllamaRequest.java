package com.clinic.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OllamaRequest {

    private String model;

    private List<Message> messages;

    private boolean stream;

    private Object format;

    private OllamaOptions options;

    public OllamaRequest(String model, List<Message> messages, boolean stream) {
        this.model = model;
        this.messages = messages;
        this.stream = stream;
    }
}
