package com.clinic.repository;

import com.clinic.domain.entity.MedicalRecord;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class MedicalRecordRepository implements PanacheRepository<MedicalRecord> {

    // Tìm kiếm bệnh án theo ID lịch hẹn (Check xem lịch hẹn này đã lập bệnh án chưa)
    public Optional<MedicalRecord> findByAppointmentId(Long appointmentId) {
        return find("appointment.id = ?1 and isDeleted = false", appointmentId).firstResultOptional();
    }

    // Tìm kiếm bệnh án active theo ID bản ghi
    public Optional<MedicalRecord> findByIdAndNotDeleted(Long id) {
        return find("id = ?1 and isDeleted = false", id).firstResultOptional();
    }

    // Tìm danh sách bệnh án theo ID bệnh nhân và chưa bị xóa
    public List<MedicalRecord> findByPatientId(Long patientId) {
        return list("appointment.patient.id = ?1 and isDeleted = false order by id desc", patientId);
    }
}