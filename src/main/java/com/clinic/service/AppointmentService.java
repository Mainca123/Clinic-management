package com.clinic.service;

import com.clinic.constant.AppointmentStatus;
import com.clinic.domain.dto.AppointmentRequest;
import com.clinic.domain.dto.AppointmentResponse;
import com.clinic.domain.entity.Appointment;
import com.clinic.domain.entity.Doctor;
import com.clinic.domain.entity.User;
import com.clinic.domain.mapper.AppointmentMapper;
import com.clinic.exception.ClinicException;
import com.clinic.exception.ErrorMessage;
import com.clinic.repository.AppointmentRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class AppointmentService {
    @Inject
    AppointmentRepository appointmentRepository;
    @Inject
    AppointmentMapper appointmentMapper;

    @Transactional // Bắt buộc để thực hiện lưu (persist) vào database
    public AppointmentResponse createAppointment(AppointmentRequest request) {

        // 1. Kiểm tra trùng lịch của bác sĩ
        // Lưu ý: Dùng request.getDoctorId() là đúng vì lúc này ta chưa có đối tượng Doctor
        if (appointmentRepository.isDoctorBusy(request.getDoctorId(),
                request.getAppointmentDate(),
                request.getStartTime())) {
            throw new ClinicException(ErrorMessage.Appointment.SCHEDULE_CONFLICT, Response.Status.BAD_REQUEST);
        }

        // 2. Sử dụng Mapper để tạo khung Entity
        Appointment entity = appointmentMapper.toEntity(request);

        // --- BƯỚC BỔ SUNG QUAN TRỌNG ĐỂ SỬA LỖI NULL ---
        // Tìm Patient từ DB (dùng Panache findById)
        User patient = User.findById(request.getPatientId());
        if (patient == null) {
            throw new ClinicException(ErrorMessage.Appointment.PATIENT_NOT_FOUND, Response.Status.NOT_FOUND);
        }
        entity.setPatient(patient); // Gán đối tượng User vào

        // Tìm Doctor từ DB
        Doctor doctor = Doctor.findById(request.getDoctorId());
        if (doctor == null) {
            throw new ClinicException(ErrorMessage.Appointment.DOCTOR_NOT_FOUND, Response.Status.NOT_FOUND);
        }
        entity.setDoctor(doctor); // Gán đối tượng Doctor vào
        // ----------------------------------------------

        // >>> BƯỚC SỬA: GÁN THÔNG TIN TRIỆU CHỨNG CHO ENTITY <<<
        // Lưu ý: Bạn hãy kiểm tra xem trong class Appointment (Entity) tên thuộc tính là getSymptom() hay getSymptoms() để gọi chính xác nhé.
        if (request.getSymptoms() == null || request.getSymptoms().trim().isEmpty()) {
            entity.setSymptoms("Không có ghi chú"); // Hiển thị chuỗi mặc định nếu trống
        } else {
            entity.setSymptoms(request.getSymptoms().trim());
        }

        // 3. Thiết lập các giá trị mặc định
        entity.setStatus(AppointmentStatus.PENDING);
        entity.setIsDeleted(false);

        // 4. Lưu vào Database
        appointmentRepository.persist(entity);

        // 5. Trả về Response
        return appointmentMapper.toResponse(entity);
    }


    public List<AppointmentResponse> getAppointments(Long userId, String role, int pageIndex, int pageSize) {
        Page page = Page.of(pageIndex, pageSize);
        PanacheQuery<Appointment> query;

        if ("DOCTOR".equals(role)) {
            // Nếu là bác sĩ, lọc theo doctor.user.id
            query = appointmentRepository.find("doctor.user.id = ?1 and isDeleted = false", userId);
        } else {
            // Nếu là bệnh nhân, lọc trực tiếp theo patient.id
            query = appointmentRepository.find("patient.id = ?1 and isDeleted = false", userId);
        }

        return query.page(page)
                .list()
                .stream()
                .map(appointmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentResponse updateStatus(Long id, String newStatus) {
        // 1. Tìm lịch hẹn, nếu không thấy thì báo lỗi
        Appointment appointment = appointmentRepository.findByIdOptional(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn có ID: " + id));

        // 2. Cập nhật trạng thái (Bạn có thể thêm logic kiểm tra quyền tại đây)
        try {
            appointment.setStatus(AppointmentStatus.valueOf(newStatus.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái không hợp lệ: " + newStatus);
        }

        // 3. Nếu là hủy lịch, bạn có thể set isDeleted = true (tùy yêu cầu dự án)
        if ("CANCELLED".equalsIgnoreCase(newStatus)) {
            // appointment.setIsDeleted(true);
        }

        // 4. Lưu và trả về kết quả đã map qua DTO
        return appointmentMapper.toResponse(appointment);
    }

    public AppointmentResponse getById(Long id) {
        // Tìm lịch hẹn theo ID, nếu không thấy thì quăng lỗi 404
        Appointment appointment = appointmentRepository.findByIdOptional(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn với ID: " + id));

        // Kiểm tra nếu lịch hẹn đã bị xóa mềm (tùy chọn theo logic của bạn)
        if (Boolean.TRUE.equals(appointment.getIsDeleted())) {
            throw new RuntimeException("Lịch hẹn này đã bị xóa.");
        }

        // Chuyển đổi Entity sang Response DTO
        return appointmentMapper.toResponse(appointment);
    }

    @Transactional // Bắt buộc có để cập nhật dữ liệu xuống DB
    public void deleteAppointmentStatus(Long id) {
        // 1. Tìm lịch hẹn theo ID, nếu không thấy hoặc đã xóa rồi thì báo lỗi 404
        Appointment appointment = appointmentRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ClinicException(
                        ErrorMessage.Appointment.PATIENT_NOT_FOUND, // Hoặc mã lỗi không tìm thấy lịch hẹn phù hợp
                        Response.Status.NOT_FOUND
                ));

        // 2. Thực hiện xóa mềm hệ thống và cập nhật trạng thái hủy lịch
        appointment.setIsDeleted(true);
        appointment.setDeletedAt(java.time.Instant.now()); // Ghi nhận thời gian xóa lịch

        // Nếu bạn có Enum trạng thái lịch hẹn (ví dụ: CANCELLED), hãy cập nhật nó tại đây
        // appointment.setStatus(AppointmentStatus.CANCELLED);

        // Kết thúc method, Quarkus Hibernate Panache sẽ tự động đồng bộ xuống Database
    }
}
