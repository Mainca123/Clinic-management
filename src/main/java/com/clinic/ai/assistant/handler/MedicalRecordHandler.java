package com.clinic.ai.assistant.handler;

import com.clinic.ai.constant.IntentType;
import com.clinic.ai.dto.ChatResponse;
import com.clinic.ai.dto.IntentResult;
import com.clinic.ai.dto.UserContext;
import com.clinic.ai.tool.MedicalRecordTool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MedicalRecordHandler {
    @Inject
    MedicalRecordTool medicalRecordTool;

    public ChatResponse handle(IntentType intent, IntentResult result, UserContext user) {
        return switch (intent) {
            case VIEW_MEDICAL_RECORD -> medicalRecordTool.view(result, user);
            case EXPLAIN_MEDICAL_RECORD -> medicalRecordTool.explain(result, user);
            case VIEW_PRESCRIPTION -> medicalRecordTool.prescription(result, user);
            case CHECK_REEXAMINATION -> medicalRecordTool.checkReexamination(user);
            case SUMMARIZE_PATIENT_HISTORY -> medicalRecordTool.summarize(result, user);
            default -> throw new IllegalArgumentException("Intent bệnh án không hợp lệ.");
        };
    }
}
