package com.clinic.ai.assistant.handler;

import com.clinic.ai.dto.ChatResponse;
import com.clinic.ai.tool.GeneralChatTool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GeneralChatHandler {

    @Inject
    GeneralChatTool generalChatTool;

    public ChatResponse handle(String message) {
        return generalChatTool.chat(message);
    }
}