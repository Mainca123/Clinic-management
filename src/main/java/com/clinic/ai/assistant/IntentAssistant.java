package com.clinic.ai.assistant;

import com.clinic.ai.assistant.handler.BookAppointmentHandler;
import com.clinic.ai.assistant.handler.FindDoctorHandler;
import com.clinic.ai.assistant.handler.GeneralChatHandler;
import com.clinic.ai.client.OllamaClient;
import com.clinic.ai.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@ApplicationScoped
public class IntentAssistant {

    @Inject
    PromptBuilder promptBuilder;

    @Inject
    @RestClient
    OllamaClient ollamaClient;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    FindDoctorHandler findDoctorHandler;

    @Inject
    BookAppointmentHandler bookAppointmentHandler;

    @Inject
    GeneralChatHandler generalChatHandler;

    public IntentResult detectIntent(String userQuestion) {

        try {

            String prompt = promptBuilder.buildIntentPrompt(userQuestion);

            OllamaRequest request = new OllamaRequest();
            request.setModel("qwen2.5:3b");
            request.setStream(false);
            request.setMessages(List.of(
                    new Message("user", prompt)
            ));

            OllamaResponse response = ollamaClient.chat(request);

            String content = response.getMessage().getContent();

            System.out.println("========== AI ==========");
            System.out.println(content);
            System.out.println("========================");

            return objectMapper.readValue(content, IntentResult.class);

        } catch (Exception e) {

            e.printStackTrace();

            IntentResult result = new IntentResult();
            result.setIntent("UNKNOWN");
            return result;
        }
    }

    public ChatResponse chat(String message) {

        IntentResult result = detectIntent(message);

        switch (result.getIntent()) {

            case "FIND_DOCTOR":
                return findDoctorHandler.handle(result);

            case "BOOK_APPOINTMENT":
                return bookAppointmentHandler.handle(result);

            case "CANCEL_APPOINTMENT":
                return new ChatResponse(
                        "Chức năng hủy lịch khám đang được phát triển.",
                        "CANCEL_APPOINTMENT",
                        false,
                        null,
                        null
                );

            case "VIEW_MEDICAL_RECORD":
                return new ChatResponse(
                        "Chức năng xem hồ sơ bệnh án đang được phát triển.",
                        "VIEW_MEDICAL_RECORD",
                        false,
                        null,
                        null
                );

            case "SYSTEM_SUPPORT":
                return new ChatResponse(
                        "Chức năng hỗ trợ hệ thống đang được phát triển.",
                        "SYSTEM_SUPPORT",
                        true,
                        null,
                        null
                );

            case "GENERAL_CHAT":
                return generalChatHandler.handle(message);

            default:
                return new ChatResponse(
                        """
                        Xin lỗi, tôi chưa hiểu rõ yêu cầu của bạn !!!\n\n
                
                        Hiện tôi có thể hỗ trợ:\n
                        • Tư vấn chuyên khoa dựa trên triệu chứng.\n
                        • Gợi ý bác sĩ phù hợp.\n
                        • Hỗ trợ đặt lịch khám.\n
                        • Giải đáp thông tin về phòng khám.\n\n
                
                        Bạn có thể mô tả triệu chứng hoặc đặt câu hỏi liên quan đến phòng khám để tôi hỗ trợ.
                        """,
                        "UNKNOWN",
                        false,
                        null,
                        null
                );
        }
    }
}