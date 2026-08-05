package com.clinic.ai.assistant;

import com.clinic.ai.assistant.handler.*;
import com.clinic.ai.assistant.router.IntentRouter;
import com.clinic.ai.constant.IntentType;
import com.clinic.ai.dto.*;
import com.clinic.ai.exception.AiAccessException;
import com.clinic.ai.exception.AiValidationException;
import com.clinic.ai.service.ConversationService;
import com.clinic.ai.service.UserContextService;
import com.clinic.ai.tool.AppointmentTool;
import com.clinic.ai.util.TextNormalizer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

@ApplicationScoped
public class IntentAssistant {
    private static final Logger LOG = Logger.getLogger(IntentAssistant.class);

    @Inject IntentRouter intentRouter;
    @Inject UserContextService userContextService;
    @Inject ConversationService conversationService;
    @Inject AppointmentTool appointmentTool;
    @Inject FindDoctorHandler findDoctorHandler;
    @Inject BookAppointmentHandler bookAppointmentHandler;
    @Inject CancelAppointmentHandler cancelAppointmentHandler;
    @Inject RescheduleAppointmentHandler rescheduleAppointmentHandler;
    @Inject AppointmentQueryHandler appointmentQueryHandler;
    @Inject MedicalRecordHandler medicalRecordHandler;
    @Inject GeneralChatHandler generalChatHandler;
    @Inject SupportHandler supportHandler;
    @Inject DoctorAssistantHandler doctorAssistantHandler;
    @Inject AdminStatisticsHandler adminStatisticsHandler;

    public ChatResponse chat(ChatRequest request) {
        String message = request == null ? null : request.getMessage();
        if (message == null || message.isBlank()) {
            return ChatResponse.failure("Tin nhắn không được để trống.", "VALIDATION");
        }

        UserContext user = userContextService.current();
        ConversationContext conversation;
        try {
            conversation = conversationService.resolve(request.getConversationId(), user);
        } catch (IllegalArgumentException exception) {
            return ChatResponse.failure(exception.getMessage(), "CONVERSATION");
        }

        try {
            PendingAction pending = conversationService.getPending(conversation);
            if (pending != null && isReject(message)) {
                conversationService.clearPending(conversation);
                return withMetadata(
                        ChatResponse.success("Đã hủy yêu cầu đang chờ xác nhận.", "CANCEL_PENDING_ACTION", null),
                        conversation,
                        IntentType.UNKNOWN,
                        null
                );
            }
            if (pending != null && isConfirm(message)) {
                ChatResponse response = appointmentTool.execute(pending, user);
                conversationService.clearPending(conversation);
                return withMetadata(response, conversation, IntentType.from(response.getTool()), null);
            }

            IntentResult result = intentRouter.route(message);
            IntentType intent = IntentType.from(result.getIntent());
            ChatResponse response = dispatch(intent, result, message, user, conversation);
            return withMetadata(response, conversation, intent, result);
        } catch (AiAccessException | AiValidationException exception) {
            return withMetadata(
                    ChatResponse.failure(exception.getMessage(), "AI_REQUEST"),
                    conversation,
                    IntentType.UNKNOWN,
                    null
            );
        } catch (Exception exception) {
            LOG.errorf("Lỗi xử lý AI: %s", exception.getClass().getSimpleName());
            return withMetadata(
                    ChatResponse.failure("Hệ thống AI đang bận. Bạn vui lòng thử lại hoặc sử dụng chức năng trực tiếp trên giao diện.", "AI_ERROR"),
                    conversation,
                    IntentType.UNKNOWN,
                    null
            );
        }
    }

    private ChatResponse dispatch(IntentType intent, IntentResult result, String message,
                                  UserContext user, ConversationContext conversation) {
        return switch (intent) {
            case FIND_DOCTOR -> findDoctorHandler.handle(result);
            case CHECK_AVAILABILITY, VIEW_APPOINTMENTS -> appointmentQueryHandler.handle(intent, result, user);
            case BOOK_APPOINTMENT -> bookAppointmentHandler.handle(result, user, conversation);
            case CANCEL_APPOINTMENT -> cancelAppointmentHandler.handle(result, user, conversation);
            case RESCHEDULE_APPOINTMENT -> rescheduleAppointmentHandler.handle(result, user, conversation);
            case VIEW_MEDICAL_RECORD, EXPLAIN_MEDICAL_RECORD, VIEW_PRESCRIPTION,
                    CHECK_REEXAMINATION, SUMMARIZE_PATIENT_HISTORY -> medicalRecordHandler.handle(intent, result, user);
            case DOCTOR_DAILY_SCHEDULE, DRAFT_MEDICAL_RECORD -> doctorAssistantHandler.handle(intent, message, user);
            case ADMIN_STATISTICS -> adminStatisticsHandler.handle(user);
            case SYSTEM_SUPPORT -> supportHandler.handle(message);
            case GENERAL_CHAT -> generalChatHandler.handle(message);
            case UNKNOWN -> unknown();
        };
    }

    private ChatResponse unknown() {
        return ChatResponse.failure(
                "Tôi chưa hiểu rõ yêu cầu. Tôi có thể gợi ý chuyên khoa và bác sĩ, hỗ trợ đặt hoặc hủy lịch, tra cứu bệnh án và hướng dẫn sử dụng hệ thống.",
                "UNKNOWN"
        );
    }

    private ChatResponse withMetadata(ChatResponse response, ConversationContext conversation,
                                      IntentType intent, IntentResult result) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        if (response.getMetadata() != null) {
            metadata.putAll(response.getMetadata());
        }
        metadata.put("conversationId", conversation.getId());
        metadata.put("intent", intent.name());
        metadata.put("requiresConfirmation", conversationService.getPending(conversation) != null);
        if (result != null && result.getConfidence() != null) {
            metadata.put("confidence", result.getConfidence());
        }
        if (intent == IntentType.FIND_DOCTOR && response.getPayload() instanceof DoctorSuggestionPayload payload) {
            metadata.put("urgency", payload.getUrgency());
        } else if (result != null && intent == IntentType.FIND_DOCTOR) {
            metadata.put("urgency", result.getUrgency());
        }
        response.setMetadata(metadata);
        return response;
    }

    private boolean isConfirm(String message) {
        String text = TextNormalizer.normalize(message);
        return text.equals("xac nhan") || text.equals("dong y") || text.equals("ok")
                || text.equals("tiep tuc") || text.equals("dung hay lam di");
    }

    private boolean isReject(String message) {
        String text = TextNormalizer.normalize(message);
        return text.equals("khong") || text.equals("khong xac nhan") || text.equals("huy thao tac")
                || text.equals("thoi") || text.equals("bo qua");
    }
}
