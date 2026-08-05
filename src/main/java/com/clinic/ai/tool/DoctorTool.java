package com.clinic.ai.tool;

import com.clinic.ai.client.OllamaClient;
import com.clinic.ai.dto.ChatResponse;
import com.clinic.ai.dto.DoctorSuggestionPayload;
import com.clinic.ai.dto.Message;
import com.clinic.ai.dto.OllamaRequest;
import com.clinic.ai.dto.OllamaResponse;
import com.clinic.domain.dto.DepartmentResponse;
import com.clinic.domain.dto.DoctorResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.List;

@ApplicationScoped
public class DoctorTool {

    @Inject
    @RestClient
    OllamaClient ollamaClient;

    public String inferDepartment(String symptoms,
                                  List<DepartmentResponse> departments) {

        StringBuilder prompt = new StringBuilder();

        prompt.append("""
                Bạn là AI hỗ trợ phân luồng khám bệnh.

                Nhiệm vụ:

                - Dựa vào triệu chứng.
                - Chỉ chọn MỘT chuyên khoa phù hợp nhất.
                - Chỉ được chọn trong danh sách dưới đây.
                - Không tự tạo tên mới.
                - Chỉ trả về đúng tên chuyên khoa.

                Danh sách chuyên khoa:

                """);

        for (DepartmentResponse department : departments) {
            prompt.append("- ")
                    .append(department.getName())
                    .append(": ")
                    .append(department.getDescription())
                    .append("\n");
        }

        prompt.append("\nTriệu chứng: ")
                .append(symptoms);

        System.out.println("Symptoms = " + symptoms);
        System.out.println(symptoms.getBytes(StandardCharsets.UTF_8).length);

        OllamaRequest request = new OllamaRequest(
                "qwen2.5:3b",
                List.of(new Message("user", prompt.toString())),
                false
        );

        OllamaResponse response = ollamaClient.chat(request);

        return response.getMessage().getContent().trim();
    }

    public ChatResponse buildResponse(String symptoms,
                                      String department,
                                      List<DoctorResponse> doctors) {

        DoctorSuggestionPayload payload = DoctorSuggestionPayload.builder()
                .department(department)
                .urgency(getUrgency(symptoms))
                .advice(getAdvice(symptoms, department))
                .doctors(doctors)
                .build();

        if (doctors.isEmpty()) {
            return new ChatResponse(
                    "Tôi xác định chuyên khoa phù hợp là " + department +
                            ". Tuy nhiên hiện chưa có bác sĩ thuộc chuyên khoa này.",
                    "DOCTOR_TOOL",
                    true,
                    payload,
                    null
            );
        }

        return new ChatResponse(
                "Tôi đề xuất bạn khám chuyên khoa " + department + ".",
                "DOCTOR_TOOL",
                true,
                payload,
                null
        );
    }

    public String getUrgency(String symptoms) {
        return "";
    }

    public String getAdvice(String symptoms,
                            String department) {
        return "";
    }
}