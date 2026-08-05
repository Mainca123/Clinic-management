package com.clinic.ai.assistant;

import com.clinic.ai.service.OllamaService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ResponseAssistant {
    @Inject
    OllamaService ollamaService;

    @Inject
    PromptBuilder promptBuilder;

    public String explainMedicalData(String data) {
        return ollamaService.text(promptBuilder.explanationSystemPrompt(), data, 420)
                .orElse("Không thể tạo phần giải thích tự động lúc này. Bạn vẫn có thể xem dữ liệu bệnh án gốc và trao đổi trực tiếp với bác sĩ điều trị.");
    }

    public String summarizeMedicalHistory(String data) {
        return ollamaService.text(promptBuilder.explanationSystemPrompt(),
                        "Tóm tắt lịch sử sau theo trình tự thời gian, không thêm dữ kiện:\n" + data, 500)
                .orElse("Không thể tạo bản tóm tắt tự động lúc này.");
    }

    public String draftMedicalRecord(String source) {
        return ollamaService.text(promptBuilder.doctorDraftSystemPrompt(), source, 500)
                .orElse("Không thể tạo bản nháp lúc này. Dữ liệu gốc của bác sĩ chưa được thay đổi.");
    }
}
