package com.clinic.ai.tool;

import com.clinic.ai.constant.UrgencyLevel;
import com.clinic.ai.dto.ChatResponse;
import com.clinic.ai.dto.DoctorSuggestionPayload;
import com.clinic.ai.dto.IntentResult;
import com.clinic.ai.service.DepartmentInferenceService;
import com.clinic.ai.service.SafetyTriageService;
import com.clinic.domain.dto.DoctorResponse;
import com.clinic.domain.entity.Department;
import com.clinic.domain.entity.Doctor;
import com.clinic.repository.DepartmentRepository;
import com.clinic.repository.DoctorRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class DoctorTool {
    @Inject
    DepartmentRepository departmentRepository;

    @Inject
    DoctorRepository doctorRepository;

    @Inject
    SafetyTriageService safetyTriageService;

    @Inject
    DepartmentInferenceService departmentInferenceService;

    @Transactional
    public ChatResponse suggest(IntentResult result) {
        String symptoms = stringParam(result.getParams(), "symptoms");
        UrgencyLevel urgency = safetyTriageService.assess(symptoms);
        String advice = safetyTriageService.advice(urgency);

        Optional<Department> department = resolveDepartment(result, symptoms);
        if (department.isEmpty()) {
            DoctorSuggestionPayload payload = DoctorSuggestionPayload.builder()
                    .urgency(urgency.name())
                    .advice(advice)
                    .confidence(result.getConfidence())
                    .doctors(List.of())
                    .build();
            return new ChatResponse(
                    "Tôi chưa xác định chắc chắn chuyên khoa phù hợp. Bạn hãy mô tả rõ vị trí khó chịu, thời điểm bắt đầu và mức độ triệu chứng.",
                    "FIND_DOCTOR",
                    false,
                    payload,
                    null
            );
        }

        Department selected = department.get();
        List<DoctorResponse> doctors = doctorRepository.fileByDepartmentId(selected.id)
                .stream()
                .filter(this::isActive)
                .sorted(Comparator.comparing(
                        Doctor::getExperienceYears,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .limit(5)
                .map(this::toResponse)
                .toList();

        DoctorSuggestionPayload payload = DoctorSuggestionPayload.builder()
                .departmentId(selected.id)
                .department(selected.getName())
                .urgency(urgency.name())
                .advice(advice)
                .confidence(result.getConfidence())
                .doctors(doctors)
                .build();

        String message = doctors.isEmpty()
                ? "Chuyên khoa phù hợp là " + selected.getName() + ", nhưng hiện chưa có bác sĩ đang hoạt động trong khoa này."
                : "Tôi gợi ý bạn khám chuyên khoa " + selected.getName() + ". " + advice;

        return ChatResponse.success(message, "FIND_DOCTOR", payload);
    }

    private Optional<Department> resolveDepartment(IntentResult result, String symptoms) {
        if (result.getDepartmentId() != null) {
            Optional<Department> byId = departmentRepository.findByIdNotDeleted(result.getDepartmentId());
            if (byId.isPresent()) {
                return byId;
            }
        }
        String name = stringParam(result.getParams(), "department");
        if (name != null) {
            Optional<Department> byName = departmentRepository
                    .find("lower(name) = lower(?1) and isDeleted = false", name.trim())
                    .firstResultOptional();
            if (byName.isPresent()) {
                return byName;
            }
        }
        List<Department> departments = departmentRepository.list("isDeleted = false order by name asc");
        return departmentInferenceService.infer(symptoms, departments);
    }

    private boolean isActive(Doctor doctor) {
        return !Boolean.TRUE.equals(doctor.getIsDeleted())
                && doctor.getUser() != null
                && !Boolean.TRUE.equals(doctor.getUser().getIsDeleted());
    }

    private DoctorResponse toResponse(Doctor doctor) {
        return DoctorResponse.builder()
                .id(doctor.id)
                .fullName(doctor.getUser().getFullName())
                .departmentName(doctor.getDepartment().getName())
                .specialization(doctor.getSpecialization())
                .experienceYears(doctor.getExperienceYears())
                .build();
    }

    private String stringParam(Map<String, Object> params, String key) {
        if (params == null || params.get(key) == null) {
            return null;
        }
        String value = params.get(key).toString().trim();
        return value.isEmpty() ? null : value;
    }
}
