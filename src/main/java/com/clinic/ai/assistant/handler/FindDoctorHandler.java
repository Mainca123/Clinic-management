package com.clinic.ai.assistant.handler;

import com.clinic.ai.dto.ChatResponse;
import com.clinic.ai.dto.IntentResult;
import com.clinic.ai.tool.DoctorTool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class FindDoctorHandler {
    @Inject
    DoctorTool doctorTool;

    public ChatResponse handle(IntentResult result) {
        return doctorTool.suggest(result);
    }
}
