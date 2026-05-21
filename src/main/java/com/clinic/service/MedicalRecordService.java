package com.clinic.service;

import com.clinic.domain.dto.*;
import com.clinic.domain.entity.Appointment;
import com.clinic.domain.entity.MedicalRecord;
import com.clinic.domain.entity.Medicine;
import com.clinic.domain.entity.PrescriptionDetail;
import com.clinic.domain.mapper.MedicalRecordMapper;
import com.clinic.exception.ClinicException;
import com.clinic.exception.ErrorMessage;
import com.clinic.repository.MedicalRecordRepository;
import com.clinic.repository.MedicineRepository;
import com.clinic.repository.PrescriptionDetailRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class MedicalRecordService {

    @Inject
    MedicalRecordRepository medicalRecordRepository;

    @Inject
    PrescriptionDetailRepository prescriptionDetailRepository;

    @Inject
    MedicalRecordMapper medicalRecordMapper;

    @Inject
    MedicineRepository MedicineRepository;

    @Transactional // Đảm bảo nếu một dòng thuốc lỗi thì toàn bộ bệnh án sẽ rollback (không lưu gì hết)
    public MedicalRecordResponse createRecord(MedicalRecordRequest request) {

        // 1. KIỂM TRA LỖI: Lịch hẹn có tồn tại không?
        // (Giả định bạn đã có Appointment Entity quản lý bằng PanacheActive Record hoặc Repository)
        Appointment appointment = Appointment.findById(request.getAppointmentId());
        if (appointment == null) {
            throw new ClinicException(ErrorMessage.Appointment.SCHEDULE_CONFLICT, Response.Status.NOT_FOUND);
        }

        // 2. KIỂM TRA LỖI: Lịch hẹn này đã được tạo bệnh án trước đó chưa? (Mối quan hệ 1-1)
        if (medicalRecordRepository.findByAppointmentId(request.getAppointmentId()).isPresent()) {
            throw new ClinicException(ErrorMessage.MedicalRecord.NOT_FOUND, Response.Status.BAD_REQUEST);
        }

        // 3. Sử dụng Mapper chuyển đổi Request sang Entity Bệnh Án
        MedicalRecord medicalRecord = medicalRecordMapper.toEntity(request);
        medicalRecord.setAppointment(appointment); // Gán object liên kết thủ công

        // Lưu thông tin Bệnh án xuống Database để sinh ra ID bệnh án trước
        medicalRecordRepository.persist(medicalRecord);

        List<PrescriptionDetailResponse> savedMedicinesResponse = new ArrayList<>();

        // 4. KIỂM TRA LỖI & LƯU DANH SÁCH THUỐC (Nếu có kê đơn)
        if (request.getMedicines() != null && !request.getMedicines().isEmpty()) {
            for (PrescriptionItemRequest item : request.getMedicines()) {

                // Kiểm tra xem ID thuốc truyền lên có tồn tại trong danh mục thuốc không
                Medicine medicine = Medicine.findById(item.getMedicineId());
                if (medicine == null || Boolean.TRUE.equals(medicine.getIsDeleted())) {
                    throw new ClinicException(ErrorMessage.Medicine.NOT_FOUND + " (ID: " + item.getMedicineId() + ")", Response.Status.NOT_FOUND);
                }

                // Khởi tạo thực thể Chi tiết đơn thuốc
                PrescriptionDetail detail = new PrescriptionDetail();
                detail.setMedicalRecord(medicalRecord); // Liên kết ngược lại với bệnh án vừa tạo phía trên
                detail.setMedicine(medicine);
                detail.setQuantity(item.getQuantity());
                detail.setDosage(item.getDosage());

                // Lưu từng dòng thuốc vào database
                prescriptionDetailRepository.persist(detail);

                // Map dữ liệu vừa lưu thành Response item để chuẩn bị trả về
                savedMedicinesResponse.add(medicalRecordMapper.toPrescriptionResponse(detail));
            }
        }

        // 5. Đóng gói dữ liệu trả về trọn vẹn thông qua đối tượng Response DTO
        MedicalRecordResponse response = medicalRecordMapper.toResponse(medicalRecord);
        response.setPrescriptionDetails(savedMedicinesResponse);

        return response;
    }

    // Lấy chi tiết bệnh án theo ID
    public MedicalRecordResponse getRecordById(Long id) {

        // 1. KIỂM TRA LỖI: Bệnh án có tồn tại và chưa bị xóa không?
        MedicalRecord medicalRecord = medicalRecordRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ClinicException(ErrorMessage.MedicalRecord.NOT_FOUND, Response.Status.NOT_FOUND));

        // 2. Lấy danh sách chi tiết đơn thuốc thuộc về bệnh án này
        List<PrescriptionDetail> details = prescriptionDetailRepository.findByMedicalRecordId(id);

        // 3. Sử dụng Mapper để chuyển đổi danh sách chi tiết đơn thuốc sang DTO Response
        List<PrescriptionDetailResponse> medicineResponses = new ArrayList<>();
        if (details != null) {
            for (PrescriptionDetail detail : details) {
                medicineResponses.add(medicalRecordMapper.toPrescriptionResponse(detail));
            }
        }

        // 4. Map thông tin master bệnh án và gán danh sách thuốc vào
        MedicalRecordResponse response = medicalRecordMapper.toResponse(medicalRecord);
        response.setPrescriptionDetails(medicineResponses);

        return response;
    }

    // Bổ sung method này vào trong MedicalRecordService.java

    @Transactional // Đảm bảo tất cả các dòng thuốc được lưu an toàn, nếu lỗi 1 dòng sẽ tự rollback
    public List<PrescriptionDetailResponse> addPrescriptionToRecord(Long medicalRecordId, PrescriptionListRequest request) {

        // 1. Kiểm tra xem bệnh án (Medical Record) có tồn tại trong hệ thống không
        MedicalRecord medicalRecord = medicalRecordRepository.findByIdAndNotDeleted(medicalRecordId)
                .orElseThrow(() -> new ClinicException(ErrorMessage.MedicalRecord.NOT_FOUND, Response.Status.NOT_FOUND));

        List<PrescriptionDetailResponse> responseList = new ArrayList<>();

        // 2. Duyệt qua từng item thuốc được truyền lên
        for (PrescriptionItemRequest item : request.getMedicines()) {

            // Nghiệp vụ quan trọng: Kiểm tra thuốc có tồn tại và chưa bị xóa logic không (is_deleted = 0)
            Medicine medicine = MedicineRepository.findByIdAndNotDeleted(item.getMedicineId())
                    .orElseThrow(() -> new ClinicException(ErrorMessage.Medicine.NOT_FOUND + " (ID: " + item.getMedicineId() + ")", Response.Status.NOT_FOUND));

            // 3. Khởi tạo thực thể để lưu xuống database
            PrescriptionDetail detail = new PrescriptionDetail();
            detail.setMedicalRecord(medicalRecord); // Gắn vào bệnh án hiện tại
            detail.setMedicine(medicine);           // Gắn vị thuốc
            detail.setQuantity(item.getQuantity()); // Gán số lượng
            detail.setDosage(item.getDosage());     // Gán cách dùng

            // Lưu trực tiếp vào bảng prescription_details
            prescriptionDetailRepository.persist(detail);

            // 4. Map thực thể vừa lưu sang Response DTO để nạp vào danh sách trả về
            responseList.add(medicalRecordMapper.toPrescriptionResponse(detail));
        }

        return responseList;
    }

    public List<PrescriptionDetailResponse> getPrescriptionByMedicalRecordId(Long medicalRecordId) {

        // 1. Kiểm tra xem bệnh án (Medical Record) có tồn tại thực sự hay không
        medicalRecordRepository.findByIdAndNotDeleted(medicalRecordId)
                .orElseThrow(() -> new ClinicException(ErrorMessage.MedicalRecord.NOT_FOUND, Response.Status.NOT_FOUND));

        // 2. Gọi hàm repo lấy ra toàn bộ các dòng thuốc thuộc bệnh án này (chưa bị xóa)
        List<PrescriptionDetail> details = prescriptionDetailRepository.findByMedicalRecordId(medicalRecordId);

        // 3. Map danh sách thực thể sang danh sách DTO trả về cho Frontend
        return medicalRecordMapper.toPrescriptionDetailResponseList(details);
    }

    // Lấy lịch sử bệnh án theo ID bệnh nhân
    public List<MedicalRecordResponse> getRecordsByPatientId(Long patientId) {

        // 1. Lấy tất cả bệnh án của bệnh nhân này
        List<MedicalRecord> records = medicalRecordRepository.findByPatientId(patientId);

        List<MedicalRecordResponse> responseList = new ArrayList<>();

        // 2. Duyệt qua từng bệnh án để đóng gói kèm đơn thuốc
        for (MedicalRecord record : records) {
            MedicalRecordResponse recordResponse = medicalRecordMapper.toResponse(record);

            // Tìm các dòng thuốc thuộc bệnh án này
            List<PrescriptionDetail> details = prescriptionDetailRepository.findByMedicalRecordId(record.id); // Dùng record.id (chữ i thường) do PanacheEntity cung cấp

            List<PrescriptionDetailResponse> medicineResponses = new ArrayList<>();
            if (details != null) {
                for (PrescriptionDetail detail : details) {
                    // ĐÃ SỬA: Gọi đúng tên hàm định nghĩa trong Mapper của bạn
                    medicineResponses.add(medicalRecordMapper.toPrescriptionResponse(detail));
                }
            }

            recordResponse.setPrescriptionDetails(medicineResponses);
            responseList.add(recordResponse);
        }

        return responseList;
    }
}