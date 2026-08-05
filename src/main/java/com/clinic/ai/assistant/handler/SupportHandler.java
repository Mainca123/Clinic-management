package com.clinic.ai.assistant.handler;

import com.clinic.ai.dto.ChatResponse;
import com.clinic.ai.tool.SystemSupportTool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SupportHandler {
    @Inject
    SystemSupportTool systemSupportTool;

    public ChatResponse handle(String message) {
        return systemSupportTool.answer(message);
    }
}
