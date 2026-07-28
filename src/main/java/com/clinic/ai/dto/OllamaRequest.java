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

}