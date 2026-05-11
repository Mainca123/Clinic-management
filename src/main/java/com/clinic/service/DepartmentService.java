package com.clinic.service;

import com.clinic.domain.dto.DepartmentRequest;
import com.clinic.domain.entity.Department;
import com.clinic.domain.mapper.DepartmentMapper;
import com.clinic.repository.DepartmentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class DepartmentService {

    @Inject
    private DepartmentRepository departmentRepository;
    @Inject
    private DepartmentMapper departmentMapper;

    @Transactional
    public String createDepartment(DepartmentRequest request){
        Department department = departmentMapper.toDepartMent(request);
        departmentRepository.persist(department);
        return "SUCCESS";
    }
}
