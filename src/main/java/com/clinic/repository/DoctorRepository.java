package com.clinic.repository;

import com.clinic.domain.entity.Doctor;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class DoctorRepository implements PanacheRepository<Doctor> {

    public Optional<Doctor> findByUserId(Long id) {

        return find("""
                user.id = ?1
                and isDeleted = false
                """,
                id
        ).firstResultOptional();
    }

    /**
     * Tìm kiếm bác sĩ theo:
     * - họ tên
     * - email
     * - chuyên môn
     * - tên khoa
     */
    public List<Doctor> searchDoctor(String keyword, int SEARCH_LIMIT) {

        if (keyword == null || keyword.trim().length() < 2) {
            return Collections.emptyList();
        }

        String search = "%" + keyword.trim().toLowerCase() + "%";

        return find("""
                select d
                from Doctor d
                join d.user u
                join d.department dp
                where (
                    lower(u.fullName) like ?1
                    or lower(u.email) like ?1
                    or lower(d.specialization) like ?1
                    or lower(dp.name) like ?1
                )
                and d.isDeleted = false
                order by d.id desc
                """,
                search
        )
                .page(Page.ofSize(SEARCH_LIMIT))
                .list();
    }



    public Optional<Doctor> findByIdAndNotDeleted(Long id) {
        // Tìm bác sĩ có id tương ứng và trạng thái isDeleted = false (hoặc bằng 0 tùy kiểu dữ liệu của bạn)
        return find("id = ?1 and isDeleted = false", id).firstResultOptional();
    }
}

