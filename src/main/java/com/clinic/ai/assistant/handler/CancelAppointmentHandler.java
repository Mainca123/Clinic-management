package com.clinic.ai.assistant.handler;

import com.clinic.ai.dto.ChatResponse;
import com.clinic.ai.dto.ConversationContext;
import com.clinic.ai.dto.IntentResult;
import com.clinic.ai.dto.UserContext;
import com.clinic.ai.tool.AppointmentTool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CancelAppointmentHandler {
    @Inject
    AppointmentTool appointmentTool;

    public ChatResponse handle(IntentResult result, UserContext user, ConversationContext conversation) {
        return appointmentTool.prepareCancel(result, user, conversation);
    }
}
