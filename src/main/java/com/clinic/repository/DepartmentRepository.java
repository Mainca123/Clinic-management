package com.clinic.repository;

import com.clinic.domain.entity.Department;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class DepartmentRepository implements PanacheRepository<Department> {

    public Optional<Department> findDepartmentById(Long id) {

        return find("""
                id = ?1
                and isDeleted = false
                """,
                id
        ).firstResultOptional();
    }



    public List<Department> searchDepartment(
            String keyword,
            int searchLimit
    ) {

        if (keyword == null || keyword.trim().length() < 2) {
            return Collections.emptyList();
        }

        String search = "%" + keyword.trim().toLowerCase() + "%";

        return find("""
                (
                    lower(name) like ?1
                    or lower(description) like ?1
                )
                and isDeleted = false
                """,
                search
        )
                .page(Page.ofSize(searchLimit))
                .list();
    }


    // Tìm khoa theo ID và đảm bảo chưa bị xóa mềm
    public Optional<Department> findByIdNotDeleted(Long id) {
        return find("id = ?1 and isDeleted = false", id).firstResultOptional();
    }
}

