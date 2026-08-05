package com.clinic.ai.tool;

import com.clinic.ai.assistant.ResponseAssistant;
import com.clinic.ai.dto.*;
import com.clinic.ai.exception.AiAccessException;
import com.clinic.ai.exception.AiValidationException;
import com.clinic.constant.RoleType;
import com.clinic.domain.dto.MedicalRecordResponse;
import com.clinic.domain.dto.PrescriptionDetailResponse;
import com.clinic.domain.entity.MedicalRecord;
import com.clinic.domain.entity.PrescriptionDetail;
import com.clinic.domain.mapper.MedicalRecordMapper;
import com.clinic.repository.MedicalRecordRepository;
import com.clinic.repository.PrescriptionDetailRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class MedicalRecordTool {
    @Inject
    MedicalRecordRepository medicalRecordRepository;

    @Inject
    PrescriptionDetailRepository prescriptionDetailRepository;

    @Inject
    MedicalRecordMapper medicalRecordMapper;

    @Inject
    ResponseAssistant responseAssistant;

    @Transactional
    public ChatResponse view(IntentResult result, UserContext user) {
        requireAuthenticated(user);
        Long recordId = longParam(result, "medicalRecordId");
        if (recordId != null) {
            MedicalRecord record = accessibleRecord(recordId, user);
            MedicalRecordResponse response = map(record);
            PatientPayload payload = PatientPayload.builder()
                    .patientId(record.getAppointment().getPatient().id)
                    .records(List.of(response))
                    .reexaminationDate(response.getReexaminationDate())
                    .build();
            return ChatResponse.success("Đây là bệnh án mã " + recordId + ".", "VIEW_MEDICAL_RECORD", payload);
        }

        Long patientId = resolvePatientId(result, user);
        List<MedicalRecordResponse> records = accessibleRecords(patientId, user)
                .stream().map(this::map).toList();
        PatientPayload payload = PatientPayload.builder()
                .patientId(patientId)
                .records(records)
                .build();
        String message = records.isEmpty()
                ? "Không tìm thấy bệnh án phù hợp với quyền truy cập của bạn."
                : "Tôi đã tìm thấy " + records.size() + " bệnh án.";
        return ChatResponse.success(message, "VIEW_MEDICAL_RECORD", payload);
    }

    @Transactional
    public ChatResponse explain(IntentResult result, UserContext user) {
        requireAuthenticated(user);
        Long recordId = requiredLong(result, "medicalRecordId", "Bạn vui lòng cung cấp mã bệnh án cần giải thích.");
        MedicalRecord record = accessibleRecord(recordId, user);
        MedicalRecordResponse response = map(record);
        String explanation = responseAssistant.explainMedicalData(toPrompt(response));
        PatientPayload payload = PatientPayload.builder()
                .patientId(record.getAppointment().getPatient().id)
                .records(List.of(response))
                .summary(explanation)
                .reexaminationDate(response.getReexaminationDate())
                .build();
        return ChatResponse.success(explanation, "EXPLAIN_MEDICAL_RECORD", payload);
    }

    @Transactional
    public ChatResponse prescription(IntentResult result, UserContext user) {
        requireAuthenticated(user);
        Long recordId = requiredLong(result, "medicalRecordId", "Bạn vui lòng cung cấp mã bệnh án để xem đơn thuốc.");
        accessibleRecord(recordId, user);
        List<PrescriptionDetailResponse> medicines = prescriptionDetailRepository
                .findByMedicalRecordId(recordId)
                .stream()
                .map(medicalRecordMapper::toPrescriptionResponse)
                .toList();
        String explanation = medicines.isEmpty()
                ? "Bệnh án này chưa có đơn thuốc."
                : responseAssistant.explainMedicalData(toPrescriptionPrompt(medicines));
        MedicinePayload payload = MedicinePayload.builder()
                .medicalRecordId(recordId)
                .medicines(medicines)
                .explanation(explanation)
                .warning("Không tự thay đổi thuốc hoặc liều dùng; hãy làm theo hướng dẫn của bác sĩ.")
                .build();
        return ChatResponse.success(explanation, "VIEW_PRESCRIPTION", payload);
    }

    @Transactional
    public ChatResponse checkReexamination(UserContext user) {
        requireAuthenticated(user);
        if (!user.hasRole(RoleType.Constants.PATIENT)) {
            throw new AiAccessException("Chức năng kiểm tra lịch tái khám dành cho bệnh nhân.");
        }
        List<MedicalRecord> records = medicalRecordRepository.findByPatientId(user.getUserId());
        LocalDate nextDate = records.stream()
                .map(MedicalRecord::getReexaminationDate)
                .filter(date -> date != null && !date.isBefore(LocalDate.now()))
                .min(Comparator.naturalOrder())
                .orElse(null);
        PatientPayload payload = PatientPayload.builder()
                .patientId(user.getUserId())
                .reexaminationDate(nextDate)
                .build();
        String message = nextDate == null
                ? "Bạn chưa có lịch tái khám sắp tới trong bệnh án."
                : "Lịch tái khám gần nhất của bạn là ngày " + nextDate + ".";
        return ChatResponse.success(message, "CHECK_REEXAMINATION", payload);
    }

    @Transactional
    public ChatResponse summarize(IntentResult result, UserContext user) {
        requireAuthenticated(user);
        Long patientId = resolvePatientId(result, user);
        List<MedicalRecordResponse> records = accessibleRecords(patientId, user)
                .stream().map(this::map).toList();
        if (records.isEmpty()) {
            throw new AiValidationException("Không có bệnh án phù hợp để tóm tắt.");
        }
        String source = records.stream().map(this::toPrompt).reduce((a, b) -> a + "\n---\n" + b).orElse("");
        String summary = responseAssistant.summarizeMedicalHistory(source);
        PatientPayload payload = PatientPayload.builder()
                .patientId(patientId)
                .records(records)
                .summary(summary)
                .build();
        return ChatResponse.success(summary, "SUMMARIZE_PATIENT_HISTORY", payload);
    }

    private List<MedicalRecord> accessibleRecords(Long patientId, UserContext user) {
        List<MedicalRecord> records = medicalRecordRepository.findByPatientId(patientId);
        if (user.hasRole(RoleType.Constants.ADMIN)) {
            return records;
        }
        if (user.hasRole(RoleType.Constants.PATIENT)) {
            if (!user.getUserId().equals(patientId)) {
                throw new AiAccessException("Bạn chỉ được xem bệnh án của chính mình.");
            }
            return records;
        }
        if (user.hasRole(RoleType.Constants.DOCTOR)) {
            return records.stream()
                    .filter(record -> record.getAppointment().getDoctor().getUser().id.equals(user.getUserId()))
                    .toList();
        }
        throw new AiAccessException("Bạn không có quyền xem bệnh án.");
    }

    private MedicalRecord accessibleRecord(Long recordId, UserContext user) {
        MedicalRecord record = medicalRecordRepository.findByIdAndNotDeleted(recordId)
                .orElseThrow(() -> new AiValidationException("Không tìm thấy bệnh án."));
        Long patientId = record.getAppointment().getPatient().id;
        boolean allowed = user.hasRole(RoleType.Constants.ADMIN)
                || user.hasRole(RoleType.Constants.PATIENT) && patientId.equals(user.getUserId())
                || user.hasRole(RoleType.Constants.DOCTOR)
                && record.getAppointment().getDoctor().getUser().id.equals(user.getUserId());
        if (!allowed) {
            throw new AiAccessException("Bạn không có quyền xem bệnh án này.");
        }
        return record;
    }

    private MedicalRecordResponse map(MedicalRecord record) {
        MedicalRecordResponse response = medicalRecordMapper.toResponse(record);
        response.setPrescriptionDetails(prescriptionDetailRepository.findByMedicalRecordId(record.id)
                .stream().map(medicalRecordMapper::toPrescriptionResponse).toList());
        return response;
    }

    private Long resolvePatientId(IntentResult result, UserContext user) {
        if (user.hasRole(RoleType.Constants.PATIENT)) {
            return user.getUserId();
        }
        Long patientId = longParam(result, "patientId");
        if (patientId == null) {
            throw new AiValidationException("Bạn vui lòng cung cấp mã bệnh nhân.");
        }
        return patientId;
    }

    private String toPrompt(MedicalRecordResponse record) {
        return "Mã bệnh án: " + record.getId()
                + "\nChẩn đoán đã ghi: " + safe(record.getDiagnosis())
                + "\nKế hoạch điều trị đã ghi: " + safe(record.getTreatmentPlan())
                + "\nNgày tái khám: " + record.getReexaminationDate()
                + "\nĐơn thuốc: " + toPrescriptionPrompt(record.getPrescriptionDetails());
    }

    private String toPrescriptionPrompt(List<PrescriptionDetailResponse> medicines) {
        if (medicines == null || medicines.isEmpty()) {
            return "Không có";
        }
        return medicines.stream()
                .map(item -> item.getMedicineName() + ", số lượng " + item.getQuantity()
                        + " " + safe(item.getUnit()) + ", cách dùng: " + safe(item.getDosage()))
                .reduce((a, b) -> a + "; " + b).orElse("Không có");
    }

    private String safe(Object value) {
        return value == null ? "Không có" : value.toString().replace('\n', ' ').trim();
    }

    private Long requiredLong(IntentResult result, String key, String message) {
        Long value = longParam(result, key);
        if (value == null) {
            throw new AiValidationException(message);
        }
        return value;
    }

    private Long longParam(IntentResult result, String key) {
        try {
            Object value = result.getParams() == null ? null : result.getParams().get(key);
            return value == null || value.toString().isBlank() ? null : Long.valueOf(value.toString());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private void requireAuthenticated(UserContext user) {
        if (user == null || !user.isAuthenticated()) {
            throw new AiAccessException("Bạn cần đăng nhập để xem dữ liệu bệnh án.");
        }
    }
}
