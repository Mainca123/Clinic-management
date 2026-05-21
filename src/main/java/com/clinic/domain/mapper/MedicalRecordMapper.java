package com.clinic.domain.mapper;

import com.clinic.domain.dto.MedicalRecordRequest;
import com.clinic.domain.dto.MedicalRecordResponse;
import com.clinic.domain.dto.PrescriptionDetailResponse;
import com.clinic.domain.entity.MedicalRecord;
import com.clinic.domain.entity.PrescriptionDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface MedicalRecordMapper {

    // Chuyển từ Request DTO sang Entity để chuẩn bị lưu bệnh án
    // Bỏ qua trường appointment vì chúng ta sẽ tìm và gán thủ công từ DB ở Service
    @Mapping(target = "appointment", ignore = true)
    MedicalRecord toEntity(MedicalRecordRequest request);

    // Chuyển từ Entity bệnh án sang Response DTO trả về Client
    @Mapping(target = "appointmentId", source = "appointment.id")
    @Mapping(target = "prescriptionDetails", ignore = true) // Sẽ tự map thủ công danh sách chi tiết ở Service để lấy tên thuốc
    MedicalRecordResponse toResponse(MedicalRecord entity);

    // Hàm bổ sung giúp map nhanh một dòng chi tiết thuốc đơn lẻ sang Response
    @Mapping(target = "id", source = "id")
    @Mapping(target = "medicineId", source = "medicine.id")
    @Mapping(target = "medicineName", source = "medicine.name")
    @Mapping(target = "unit", source = "medicine.unit")
    PrescriptionDetailResponse toPrescriptionResponse(PrescriptionDetail detail);

    // Thêm hàm này vào MedicalRecordMapper.java để map cả danh sách thực thể
    List<PrescriptionDetailResponse> toPrescriptionDetailResponseList(List<PrescriptionDetail> details);
}