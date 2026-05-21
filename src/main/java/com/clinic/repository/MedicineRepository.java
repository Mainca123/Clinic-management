package com.clinic.repository;

import com.clinic.domain.entity.Medicine;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class MedicineRepository implements PanacheRepository<Medicine> {

    // Tìm thuốc theo tên và chưa bị xóa (Soft Delete)
    public Optional<Medicine> findByName(String name) {
        return find("name = ?1 and isDeleted = false", name).firstResultOptional();
    }

    // Tìm thuốc theo ID và chưa bị xóa (Soft Delete)
    public Optional<Medicine> findByIdAndNotDeleted(Long id) {
        // Tìm kiếm thuốc theo ID và điều kiện isDeleted là false
        return find("id = ?1 and isDeleted = false", id).firstResultOptional();
    }

    // tìm danh sách thuốc mà cha bị xóa
    public List<Medicine> listAllActive() {
        // Lấy tất cả thuốc có isDeleted = false, sắp xếp theo tên từ A-Z
        return list("isDeleted = false order by name asc");
    }


}
