package com.clinic.ai.tool;

import com.clinic.ai.constant.PendingActionType;
import com.clinic.ai.dto.*;
import com.clinic.ai.exception.AiAccessException;
import com.clinic.ai.exception.AiValidationException;
import com.clinic.ai.service.ConversationService;
import com.clinic.ai.util.NaturalDateTimeParser;
import com.clinic.constant.AppointmentStatus;
import com.clinic.constant.RoleType;
import com.clinic.domain.dto.AppointmentRequest;
import com.clinic.domain.dto.AppointmentResponse;
import com.clinic.domain.entity.Appointment;
import com.clinic.domain.entity.Doctor;
import com.clinic.domain.mapper.AppointmentMapper;
import com.clinic.repository.AppointmentRepository;
import com.clinic.repository.DoctorRepository;
import com.clinic.service.AppointmentService;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AppointmentTool {
    private static final Duration CONFIRMATION_TTL = Duration.ofMinutes(5);

    @Inject
    AppointmentRepository appointmentRepository;

    @Inject
    DoctorRepository doctorRepository;

    @Inject
    AppointmentService appointmentService;

    @Inject
    AppointmentMapper appointmentMapper;

    @Inject
    ConversationService conversationService;

    @Transactional
    public ChatResponse view(UserContext user) {
        requireAuthenticated(user);
        if (!user.hasRole(RoleType.Constants.PATIENT) && !user.hasRole(RoleType.Constants.DOCTOR)) {
            throw new AiAccessException("Chức năng này chỉ dành cho bệnh nhân hoặc bác sĩ.");
        }
        List<AppointmentResponse> appointments = appointmentRepository
                .findByUserId(user.getUserId(), user.getRole(), Page.of(0, 20))
                .list()
                .stream()
                .map(appointmentMapper::toResponse)
                .toList();
        AppointmentPayload payload = AppointmentPayload.builder()
                .action("VIEW")
                .appointments(appointments)
                .build();
        String message = appointments.isEmpty()
                ? "Bạn chưa có lịch khám nào."
                : "Tôi đã tìm thấy " + appointments.size() + " lịch khám gần nhất.";
        return ChatResponse.success(message, "VIEW_APPOINTMENTS", payload);
    }

    @Transactional
    public ChatResponse checkAvailability(IntentResult result) {
        Long doctorId = longParam(result, "doctorId");
        LocalDate date = dateParam(result, "date");
        if (doctorId == null || date == null) {
            throw new AiValidationException("Bạn vui lòng cung cấp mã bác sĩ và ngày muốn khám.");
        }
        Doctor doctor = doctorRepository.findByIdAndNotDeleted(doctorId)
                .orElseThrow(() -> new AiValidationException("Không tìm thấy bác sĩ đang hoạt động."));
        List<LocalTime> busyTimes = appointmentRepository.findByDoctorIdAndDate(doctorId, date)
                .stream()
                .filter(item -> item.getStatus() != AppointmentStatus.CANCELLED)
                .map(Appointment::getStartTime)
                .toList();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("doctorId", doctorId);
        payload.put("doctorName", doctor.getUser().getFullName());
        payload.put("date", date);
        payload.put("bookedTimes", busyTimes);
        payload.put("note", "Hệ thống hiện chỉ trả các giờ đã được đặt; giờ làm việc của bác sĩ chưa được cấu hình.");
        return ChatResponse.success(
                "Đây là các khung giờ đã được đặt của bác sĩ trong ngày " + date + ".",
                "CHECK_AVAILABILITY",
                payload
        );
    }

    @Transactional
    public ChatResponse prepareBook(IntentResult result, UserContext user, ConversationContext conversation) {
        requirePatient(user);
        Long doctorId = longParam(result, "doctorId");
        LocalDate date = dateParam(result, "date");
        LocalTime time = timeParam(result, "time");
        String symptoms = stringParam(result, "symptoms");
        if (doctorId == null || date == null || time == null) {
            throw new AiValidationException("Để đặt lịch, bạn cần cung cấp mã bác sĩ, ngày và giờ khám.");
        }
        validateDateTime(date, time);
        Doctor doctor = doctorRepository.findByIdAndNotDeleted(doctorId)
                .orElseThrow(() -> new AiValidationException("Không tìm thấy bác sĩ đang hoạt động."));
        ensureSlotFree(doctorId, date, time, null);

        Map<String, String> params = new LinkedHashMap<>();
        params.put("doctorId", doctorId.toString());
        params.put("date", date.toString());
        params.put("time", time.toString());
        params.put("symptoms", symptoms == null ? "Không có ghi chú" : symptoms);
        PendingAction action = new PendingAction(
                PendingActionType.BOOK_APPOINTMENT,
                user.getUserId(),
                params,
                Instant.now().plus(CONFIRMATION_TTL)
        );
        conversationService.setPending(conversation, action);

        AppointmentResponse preview = AppointmentResponse.builder()
                .doctorName(doctor.getUser().getFullName())
                .appointmentDate(date)
                .startTime(time)
                .symptoms(params.get("symptoms"))
                .status(AppointmentStatus.PENDING)
                .build();
        return confirmation("BOOK", preview, action,
                "Bạn muốn đặt lịch với bác sĩ " + doctor.getUser().getFullName()
                        + " lúc " + time + " ngày " + date + ". Hãy trả lời “xác nhận” để hoàn tất.",
                "BOOK_APPOINTMENT");
    }

    @Transactional
    public ChatResponse prepareCancel(IntentResult result, UserContext user, ConversationContext conversation) {
        requireAuthenticated(user);
        Long appointmentId = longParam(result, "appointmentId");
        if (appointmentId == null) {
            return viewWithInstruction(user, "Bạn hãy chọn mã lịch hẹn cần hủy.");
        }
        Appointment appointment = ownedAppointment(appointmentId, user);
        if (appointment.getStatus() == AppointmentStatus.CANCELLED
                || appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new AiValidationException("Lịch hẹn này không thể hủy ở trạng thái hiện tại.");
        }
        PendingAction action = new PendingAction(
                PendingActionType.CANCEL_APPOINTMENT,
                user.getUserId(),
                Map.of("appointmentId", appointmentId.toString()),
                Instant.now().plus(CONFIRMATION_TTL)
        );
        conversationService.setPending(conversation, action);
        return confirmation("CANCEL", appointmentMapper.toResponse(appointment), action,
                "Bạn có chắc muốn hủy lịch mã " + appointmentId + "? Hãy trả lời “xác nhận” để hủy.",
                "CANCEL_APPOINTMENT");
    }

    @Transactional
    public ChatResponse prepareReschedule(IntentResult result, UserContext user, ConversationContext conversation) {
        requireAuthenticated(user);
        Long appointmentId = longParam(result, "appointmentId");
        LocalDate date = dateParam(result, "date");
        LocalTime time = timeParam(result, "time");
        if (appointmentId == null || date == null || time == null) {
            throw new AiValidationException("Bạn cần cung cấp mã lịch hẹn, ngày mới và giờ mới.");
        }
        validateDateTime(date, time);
        Appointment appointment = ownedAppointment(appointmentId, user);
        if (appointment.getStatus() == AppointmentStatus.CANCELLED
                || appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new AiValidationException("Lịch hẹn này không thể đổi ở trạng thái hiện tại.");
        }
        ensureSlotFree(appointment.getDoctor().id, date, time, appointment.id);
        Map<String, String> params = Map.of(
                "appointmentId", appointmentId.toString(),
                "date", date.toString(),
                "time", time.toString()
        );
        PendingAction action = new PendingAction(
                PendingActionType.RESCHEDULE_APPOINTMENT,
                user.getUserId(),
                params,
                Instant.now().plus(CONFIRMATION_TTL)
        );
        conversationService.setPending(conversation, action);
        AppointmentResponse preview = appointmentMapper.toResponse(appointment);
        preview.setAppointmentDate(date);
        preview.setStartTime(time);
        return confirmation("RESCHEDULE", preview, action,
                "Bạn muốn đổi lịch mã " + appointmentId + " sang " + time + " ngày " + date
                        + ". Hãy trả lời “xác nhận” để hoàn tất.",
                "RESCHEDULE_APPOINTMENT");
    }

    @Transactional
    public ChatResponse execute(PendingAction action, UserContext user) {
        requireAuthenticated(user);
        if (action == null || action.isExpired()) {
            throw new AiValidationException("Yêu cầu xác nhận đã hết hạn. Bạn vui lòng thực hiện lại.");
        }
        if (!user.getUserId().equals(action.getUserId())) {
            throw new AiAccessException("Bạn không có quyền xác nhận thao tác này.");
        }
        return switch (action.getType()) {
            case BOOK_APPOINTMENT -> executeBook(action, user);
            case CANCEL_APPOINTMENT -> executeCancel(action, user);
            case RESCHEDULE_APPOINTMENT -> executeReschedule(action, user);
        };
    }

    private ChatResponse executeBook(PendingAction action, UserContext user) {
        requirePatient(user);
        Long doctorId = Long.valueOf(action.getParams().get("doctorId"));
        LocalDate date = LocalDate.parse(action.getParams().get("date"));
        LocalTime time = LocalTime.parse(action.getParams().get("time"));
        ensureSlotFree(doctorId, date, time, null);
        AppointmentRequest request = new AppointmentRequest(
                user.getUserId(), doctorId, date, time, action.getParams().get("symptoms")
        );
        AppointmentResponse saved = appointmentService.createAppointment(request);
        AppointmentPayload payload = AppointmentPayload.builder()
                .action("BOOKED")
                .appointment(saved)
                .build();
        return ChatResponse.success("Đặt lịch thành công. Mã lịch hẹn của bạn là " + saved.getId() + ".",
                "BOOK_APPOINTMENT", payload);
    }

    private ChatResponse executeCancel(PendingAction action, UserContext user) {
        Long id = Long.valueOf(action.getParams().get("appointmentId"));
        ownedAppointment(id, user);
        AppointmentResponse updated = appointmentService.updateStatus(id, AppointmentStatus.CANCELLED.name());
        AppointmentPayload payload = AppointmentPayload.builder()
                .action("CANCELLED")
                .appointment(updated)
                .build();
        return ChatResponse.success("Đã hủy lịch hẹn mã " + id + ".", "CANCEL_APPOINTMENT", payload);
    }

    private ChatResponse executeReschedule(PendingAction action, UserContext user) {
        Long id = Long.valueOf(action.getParams().get("appointmentId"));
        LocalDate date = LocalDate.parse(action.getParams().get("date"));
        LocalTime time = LocalTime.parse(action.getParams().get("time"));
        Appointment appointment = ownedAppointment(id, user);
        ensureSlotFree(appointment.getDoctor().id, date, time, id);
        appointment.setAppointmentDate(date);
        appointment.setStartTime(time);
        if (appointment.getStatus() == AppointmentStatus.CONFIRMED) {
            appointment.setStatus(AppointmentStatus.PENDING);
        }
        AppointmentResponse updated = appointmentMapper.toResponse(appointment);
        AppointmentPayload payload = AppointmentPayload.builder()
                .action("RESCHEDULED")
                .appointment(updated)
                .build();
        return ChatResponse.success("Đã đổi lịch hẹn mã " + id + " sang " + time + " ngày " + date + ".",
                "RESCHEDULE_APPOINTMENT", payload);
    }

    private Appointment ownedAppointment(Long id, UserContext user) {
        Appointment appointment = appointmentRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new AiValidationException("Không tìm thấy lịch hẹn."));
        boolean allowed = user.hasRole(RoleType.Constants.ADMIN)
                || user.hasRole(RoleType.Constants.PATIENT) && appointment.getPatient().id.equals(user.getUserId())
                || user.hasRole(RoleType.Constants.DOCTOR) && appointment.getDoctor().getUser().id.equals(user.getUserId());
        if (!allowed) {
            throw new AiAccessException("Bạn không có quyền thao tác với lịch hẹn này.");
        }
        return appointment;
    }

    private void ensureSlotFree(Long doctorId, LocalDate date, LocalTime time, Long ignoredAppointmentId) {
        boolean busy = appointmentRepository.findByDoctorIdAndDate(doctorId, date)
                .stream()
                .anyMatch(item -> !item.id.equals(ignoredAppointmentId)
                        && item.getStartTime().equals(time)
                        && item.getStatus() != AppointmentStatus.CANCELLED);
        if (busy) {
            throw new AiValidationException("Khung giờ này đã có người đặt. Bạn vui lòng chọn giờ khác.");
        }
    }

    private void validateDateTime(LocalDate date, LocalTime time) {
        if (LocalDateTime.of(date, time).isBefore(LocalDateTime.now())) {
            throw new AiValidationException("Thời gian khám phải ở tương lai.");
        }
    }

    private ChatResponse confirmation(String actionName, AppointmentResponse preview, PendingAction action,
                                      String message, String tool) {
        AppointmentPayload payload = AppointmentPayload.builder()
                .action(actionName)
                .appointment(preview)
                .requiresConfirmation(true)
                .confirmationExpiresAt(action.getExpiresAt())
                .build();
        return ChatResponse.success(message, tool, payload);
    }

    private ChatResponse viewWithInstruction(UserContext user, String instruction) {
        ChatResponse response = view(user);
        response.setMessage(instruction + " " + response.getMessage());
        return response;
    }

    private void requireAuthenticated(UserContext user) {
        if (user == null || !user.isAuthenticated()) {
            throw new AiAccessException("Bạn cần đăng nhập để sử dụng chức năng này.");
        }
    }

    private void requirePatient(UserContext user) {
        requireAuthenticated(user);
        if (!user.hasRole(RoleType.Constants.PATIENT)) {
            throw new AiAccessException("Chỉ tài khoản bệnh nhân mới có thể đặt lịch bằng trợ lý AI.");
        }
    }

    private String stringParam(IntentResult result, String key) {
        Object value = result.getParams() == null ? null : result.getParams().get(key);
        if (value == null) {
            return null;
        }
        String text = value.toString().trim();
        return text.isEmpty() ? null : text;
    }

    private Long longParam(IntentResult result, String key) {
        try {
            String value = stringParam(result, key);
            return value == null ? null : Long.valueOf(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private LocalDate dateParam(IntentResult result, String key) {
        return NaturalDateTimeParser.parseDate(stringParam(result, key)).orElse(null);
    }

    private LocalTime timeParam(IntentResult result, String key) {
        return NaturalDateTimeParser.parseTime(stringParam(result, key)).orElse(null);
    }
}
