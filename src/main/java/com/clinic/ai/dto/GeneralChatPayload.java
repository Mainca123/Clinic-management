package com.clinic.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneralChatPayload {

    // Câu hỏi gốc của người dùng
    private String userMessage;

    // Câu trả lời của AI
    private String aiResponse;
}