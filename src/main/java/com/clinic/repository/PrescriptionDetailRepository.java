package com.clinic.repository;

import com.clinic.domain.entity.PrescriptionDetail;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class PrescriptionDetailRepository implements PanacheRepository<PrescriptionDetail> {

    // Lấy danh sách các dòng thuốc thuộc về một bệnh án cụ thể
    public List<PrescriptionDetail> findByMedicalRecordId(Long medicalRecordId) {
        return list("medicalRecord.id = ?1 and isDeleted = false", medicalRecordId);
    }
}