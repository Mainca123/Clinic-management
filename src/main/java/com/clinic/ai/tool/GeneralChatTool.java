package com.clinic.ai.tool;

import com.clinic.ai.assistant.PromptBuilder;
import com.clinic.ai.dto.ChatResponse;
import com.clinic.ai.dto.GeneralChatPayload;
import com.clinic.ai.service.OllamaService;
import com.clinic.ai.util.TextNormalizer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GeneralChatTool {
    @Inject
    OllamaService ollamaService;

    @Inject
    PromptBuilder promptBuilder;

    public ChatResponse chat(String message) {
        String reply = deterministicReply(message);
        if (reply == null) {
            reply = ollamaService.text(promptBuilder.generalSystemPrompt(), message, 180)
                    .orElse("Xin chào! Tôi có thể hỗ trợ tìm chuyên khoa, bác sĩ, hướng dẫn đặt lịch và tra cứu thông tin phòng khám.");
        }

        GeneralChatPayload payload = GeneralChatPayload.builder()
                .userMessage(message)
                .aiResponse(reply)
                .build();

        return ChatResponse.success(reply, "GENERAL_CHAT", payload);
    }

    private String deterministicReply(String message) {
        String normalized = TextNormalizer.normalize(message);
        if (TextNormalizer.containsAny(message, "bạn là ai", "bạn tên gì")) {
            return "Tôi là trợ lý AI của Hệ thống Quản lý Phòng khám. Tôi hỗ trợ tìm chuyên khoa, gợi ý bác sĩ, đặt hoặc hủy lịch và tra cứu bệnh án theo quyền của bạn.";
        }
        if (TextNormalizer.containsAny(message, "bạn làm được gì", "bạn có thể làm gì")) {
            return "Tôi có thể gợi ý chuyên khoa và bác sĩ, hỗ trợ đặt, xem, hủy hoặc đổi lịch, tra cứu bệnh án và hướng dẫn sử dụng hệ thống.";
        }
        if (TextNormalizer.containsAny(message, "xin chào", "chào bạn")
                || normalized.equals("hello") || normalized.equals("hi") || normalized.equals("chao")) {
            return "Xin chào! Tôi là trợ lý AI của phòng khám. Bạn cần tìm bác sĩ, đặt lịch hay tra cứu thông tin nào?";
        }
        return null;
    }
}
