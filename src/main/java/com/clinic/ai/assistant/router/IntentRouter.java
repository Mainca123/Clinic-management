package com.clinic.ai.assistant.router;

import com.clinic.ai.assistant.PromptBuilder;
import com.clinic.ai.constant.IntentType;
import com.clinic.ai.dto.IntentResult;
import com.clinic.ai.service.OllamaService;
import com.clinic.ai.util.TextNormalizer;
import com.clinic.domain.entity.Department;
import com.clinic.repository.DepartmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class IntentRouter {
    private static final Logger LOG = Logger.getLogger(IntentRouter.class);

    @Inject
    PromptBuilder promptBuilder;

    @Inject
    OllamaService ollamaService;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    DepartmentRepository departmentRepository;

    public IntentResult route(String userMessage) {
        Optional<IntentResult> fastResult = fastRoute(userMessage);
        if (fastResult.isPresent()) {
            return fastResult.get();
        }

        List<Department> departments = departmentRepository
                .list("isDeleted = false order by name asc");

        Optional<String> content = ollamaService.json(
                promptBuilder.buildRouterSystemPrompt(departments),
                userMessage
        );
        if (content.isEmpty()) {
            return unknown();
        }

        try {
            IntentResult result = objectMapper.readValue(cleanJson(content.get()), IntentResult.class);
            result.setIntent(IntentType.from(result.getIntent()).name());
            if (result.getParams() == null) {
                result.setParams(Collections.emptyMap());
            }
            if (result.getConfidence() == null) {
                result.setConfidence(0.0);
            }
            if (result.getDepartmentId() != null && departments.stream()
                    .noneMatch(item -> item.id.equals(result.getDepartmentId()))) {
                result.setDepartmentId(null);
            }
            return result;
        } catch (Exception exception) {
            LOG.warn("Không phân tích được JSON định tuyến từ Ollama.");
            return unknown();
        }
    }

    private Optional<IntentResult> fastRoute(String message) {
        String text = TextNormalizer.normalize(message);
        if (text.matches("^(xin chao|chao|hello|hi|cam on|tam biet)( ban| nhe| ai)?[ ]*$")) {
            return Optional.of(result(IntentType.GENERAL_CHAT));
        }
        if (TextNormalizer.containsAny(text,
                "bạn là ai", "bạn tên gì", "bạn làm được gì", "bạn có thể làm gì")) {
            return Optional.of(result(IntentType.GENERAL_CHAT));
        }
        if (TextNormalizer.containsAny(text,
                "hướng dẫn sử dụng", "giờ làm việc", "địa chỉ phòng khám",
                "phòng khám ở đâu", "số điện thoại", "bảo hiểm", "quy trình khám",
                "mã bác sĩ", "id bác sĩ", "tìm mã bác sĩ")) {
            return Optional.of(result(IntentType.SYSTEM_SUPPORT));
        }
        if (looksLikeSymptom(text)) {
            return Optional.of(new IntentResult(
                    IntentType.FIND_DOCTOR.name(),
                    Map.of("symptoms", message.trim()),
                    null,
                    "UNKNOWN",
                    1.0
            ));
        }
        if (TextNormalizer.containsAny(text, "lịch khám hôm nay của tôi", "lịch bệnh nhân hôm nay")) {
            return Optional.of(result(IntentType.DOCTOR_DAILY_SCHEDULE));
        }
        if (TextNormalizer.containsAny(text, "thống kê hệ thống", "thống kê lịch khám", "báo cáo hệ thống")) {
            return Optional.of(result(IntentType.ADMIN_STATISTICS));
        }
        return Optional.empty();
    }

    private boolean looksLikeSymptom(String text) {
        return TextNormalizer.containsAny(text,
                "đau tim", "đau ở tim", "đau vùng tim", "bệnh tim", "nhồi máu cơ tim",
                "đau ngực", "tức ngực", "nặng ngực", "khó chịu ở ngực",
                "khó thở", "không thở được", "hồi hộp", "tim đập nhanh",
                "đau đầu", "chóng mặt", "co giật", "méo miệng", "liệt nửa người",
                "đau bụng", "đau dạ dày", "buồn nôn", "nôn", "tiêu chảy",
                "sốt", "ho", "đau họng", "nghẹt mũi", "ù tai",
                "ngứa", "phát ban", "nổi mẩn", "dị ứng",
                "đau xương", "đau khớp", "đau lưng", "sưng khớp",
                "đau mắt", "mờ mắt", "đỏ mắt", "chảy máu", "bất tỉnh",
                "sưng chân", "sưng tay", "tê bì", "mệt mỏi", "mất ngủ",
                "tôi đang đau", "tôi thấy đau");
    }

    private IntentResult result(IntentType intent) {
        return new IntentResult(intent.name(), Map.of(), null, "UNKNOWN", 1.0);
    }

    private IntentResult unknown() {
        return new IntentResult(IntentType.UNKNOWN.name(), Map.of(), null, "UNKNOWN", 0.0);
    }

    private String cleanJson(String raw) {
        String value = raw.trim();
        if (value.startsWith("```")) {
            value = value.replaceFirst("^```(?:json)?\\s*", "")
                    .replaceFirst("\\s*```$", "");
        }
        int start = value.indexOf('{');
        int end = value.lastIndexOf('}');
        return start >= 0 && end > start ? value.substring(start, end + 1) : value;
    }
}
