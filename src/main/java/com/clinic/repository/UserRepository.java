package com.clinic.repository;

import com.clinic.domain.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public Optional<User> findByUsernameOrEmail(String identifier) {
        return find(
                "(username = :identifier or email = :identifier) and isDeleted = false",
                Parameters.with("identifier", identifier)
        ).firstResultOptional();
    }

    public Optional<User> findByEmail(String email) {
        return find("email = ?1", email)
                .firstResultOptional();
    }

    public Optional<User> findByTokenVerified(String token) {
        return find("tokenVerified = ?1", token).firstResultOptional();
    }

    public List<User> searchUserByFullName(
            String keyword,
            Long currentUserId,
            int limit
    ) {

        if (keyword == null || keyword.trim().length() < 2) {
            return Collections.emptyList();
        }

        String search = "%" + keyword.trim().toLowerCase() + "%";

        return find("""
            lower(fullName) like ?1
            and id != ?2
            and isDeleted = false
            """,
                search,
                currentUserId
        )
                .page(Page.ofSize(limit))
                .list();
    }
    public List<User> getUsers(int page, int size) {
        return findAll()
                .page(page - 0, size)
                .list();
    }

    public long countUsers() {
        return count("isDeleted = false");
    }

    // Thêm vào class UserRepository.java

    // 2. >>> ĐOẠN SỬA ĐỔI CHÍNH CHỮA LỖI SYNTAX <<<
    public List<User> getPatientsByDoctor(Long doctorUserId, int page, int size) {
        // Bắt buộc phải viết đầy đủ câu lệnh có từ khóa "select" để Hibernate hiểu ta đang lấy dữ liệu từ bảng Appointment sang
        return find("select distinct a.patient from Appointment a where a.doctor.user.id = ?1 and a.isDeleted = false", doctorUserId)
                .page(page, size)
                .list();
    }

    public long countPatientsByDoctor(Long doctorUserId) {
        // Tương tự cho hàm đếm số lượng
        return find("select distinct a.patient from Appointment a where a.doctor.user.id = ?1 and a.isDeleted = false", doctorUserId)
                .count();
    }

    // Thêm vào file UserRepository.java

    public List<User> getPatientsByDoctorId(Long doctorId, int page, int size) {
        // Tìm các User (Patient) dựa vào a.doctor.id (ID của bảng Doctor) thay vì bảng User
        return find("select distinct a.patient from Appointment a where a.doctor.id = ?1 and a.isDeleted = false", doctorId)
                .page(page, size)
                .list();
    }

    public long countPatientsByDoctorId(Long doctorId) {
        return find("select distinct a.patient from Appointment a where a.doctor.id = ?1 and a.isDeleted = false", doctorId)
                .count();
    }
}