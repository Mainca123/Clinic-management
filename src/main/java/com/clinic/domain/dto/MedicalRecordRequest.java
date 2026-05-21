package com.clinic.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class MedicalRecordRequest {

    @NotNull(message = "Mã lịch hẹn không được để trống")
    @Schema(description = "ID của lịch hẹn tương ứng", example = "201")
    private Long appointmentId;

    @NotBlank(message = "Chẩn đoán không được để trống")
    @Schema(description = "Chẩn đoán bệnh từ bác sĩ", example = "Viêm họng cấp tính")
    private String diagnosis;

    @NotBlank(message = "Kế hoạch điều trị không được để trống")
    @Schema(description = "Phác đồ hoặc lời dặn điều trị", example = "Nghỉ ngơi, súc miệng nước muối ấm")
    private String treatmentPlan;

    @Schema(description = "Ngày hẹn tái khám (nếu có)", example = "2026-05-26")
    private LocalDate reexaminationDate;

    @Valid // Kích hoạt validation cho từng item trong danh sách danh mục thuốc bên dưới
    @Schema(description = "Danh sách đơn thuốc kèm theo")
    private List<PrescriptionItemRequest> medicines;
}