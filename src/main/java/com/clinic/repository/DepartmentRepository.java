package com.clinic.repository;

import com.clinic.domain.entity.Department;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;


@ApplicationScoped
public class DepartmentRepository implements PanacheRepository<Department> {
    public Optional<Department> findDepartmentById(Long id){
        return find("id = ?1", id).firstResultOptional();
    }
}
