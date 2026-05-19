package com.clinic.repository;

import com.clinic.domain.entity.Appointment;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AppointmentRepository implements PanacheRepository<Appointment> {
    /**
     * Kiểm tra xem bác sĩ đã có lịch hẹn vào ngày và giờ cụ thể chưa
     * Trả về true nếu đã có lịch (bận), false nếu còn trống
     */
    public boolean isDoctorBusy(Long doctorId, LocalDate date, LocalTime time) {
        // Query: Đếm số bản ghi trùng khớp bác sĩ, ngày, giờ và chưa bị xóa
        return count("doctor.id = ?1 and appointmentDate = ?2 and startTime = ?3 and isDeleted = false",
                doctorId, date, time) > 0;
    }

    public PanacheQuery<Appointment> findByUserId(Long userId, String role, Page page) {
        if ("DOCTOR".equals(role)) {
            // Tìm lịch hẹn mà người này là Bác sĩ (liên kết qua bảng doctors)
            return find("doctor.user.id = ?1 and isDeleted = false", userId).page(page);
        } else {
            // Tìm lịch hẹn mà người này là Bệnh nhân
            return find("patient.id = ?1 and isDeleted = false", userId).page(page);
        }
    }

    public Optional<Appointment> findByIdAndNotDeleted(Long id) {
        return find("id = ?1 and isDeleted = false", id).firstResultOptional();
    }
}
