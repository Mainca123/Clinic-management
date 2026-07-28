package com.clinic.ai.service;

import com.clinic.ai.assistant.IntentAssistant;
import com.clinic.ai.dto.ChatResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AIService {

    @Inject
    IntentAssistant intentAssistant;

    public ChatResponse chat(String message) {
        return intentAssistant.chat(message);
    }

}