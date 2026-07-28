package com.clinic.ai.tool;

import com.clinic.ai.client.OllamaClient;
import com.clinic.ai.dto.ChatResponse;
import com.clinic.ai.dto.GeneralChatPayload;
import com.clinic.ai.dto.Message;
import com.clinic.ai.dto.OllamaRequest;
import com.clinic.ai.dto.OllamaResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@ApplicationScoped
public class GeneralChatTool {

    @Inject
    @RestClient
    OllamaClient ollamaClient;

    public ChatResponse chat(String message) {

        String prompt = """
            <|system|>
             
             Bạn là trợ lý AI của Hệ thống Quản lý Phòng khám.

             QUY TẮC BẮT BUỘC:

             1. Chỉ được trả lời bằng TIẾNG VIỆT.
             2. Tuyệt đối không được sử dụng tiếng Anh.
             3. Tuyệt đối không được sử dụng tiếng Trung.
             4. Nếu câu trả lời chứa bất kỳ từ tiếng Anh nào thì hãy viết lại hoàn toàn bằng tiếng Việt.
             5. Trả lời ngắn gọn.
               
            Vai trò của bạn:
            - Hỗ trợ người dùng sử dụng hệ thống phòng khám.
            - Trò chuyện thân thiện khi người dùng chào hỏi.
            - Chỉ trả lời bằng tiếng Việt.
            - Không sử dụng tiếng Anh hoặc tiếng Trung.
            - Trả lời ngắn gọn, tối đa 3 câu.
            
            Bạn có thể hỗ trợ:
            - Tư vấn chuyên khoa dựa trên triệu chứng.
            - Gợi ý bác sĩ phù hợp.
            - Hướng dẫn đặt lịch khám.
            - Giải đáp thông tin về phòng khám như giờ làm việc, địa chỉ, quy trình khám.
            - Trả lời các câu chào hỏi, cảm ơn, tạm biệt.
            
            Nếu người dùng hỏi ngoài phạm vi phòng khám, hãy trả lời lịch sự rằng bạn chỉ hỗ trợ các vấn đề liên quan đến hệ thống phòng khám.
            
            <|user|>
            %s
            """.formatted(message);

        OllamaRequest request = new OllamaRequest(
                "qwen2.5:3b",
                List.of(new Message("user", prompt)),
                false
        );

        OllamaResponse response = ollamaClient.chat(request);

        String reply = response.getMessage().getContent().trim();

        GeneralChatPayload payload = GeneralChatPayload.builder()
                .userMessage(message)
                .build();

        return new ChatResponse(
                reply,
                "GENERAL_CHAT",
                true,
                payload,
                null
        );
    }
}