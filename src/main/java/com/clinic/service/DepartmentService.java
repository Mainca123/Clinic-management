package com.clinic.service;

import com.clinic.domain.dto.DepartmentListResponse;
import com.clinic.domain.dto.DepartmentRequest;
import com.clinic.domain.dto.DepartmentResponse;
import com.clinic.domain.entity.Department;
import com.clinic.domain.mapper.DepartmentMapper;
import com.clinic.repository.DepartmentRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

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

    @Transactional
    public DepartmentListResponse getAllDepartment(int page){

        var query = departmentRepository.findAll();

        query.page(Page.of(page, 20));

        List<DepartmentResponse> departments = query.list()
                .stream()
                .map(department -> DepartmentResponse.builder()
                        .id(department.id)
                        .name(department.getName())
                        .description(department.getDescription())
                        .build())
                .toList();

        return DepartmentListResponse.builder()
                .departmentResponseList(departments)
                .totalItems(query.count())
                .totalPages(query.pageCount())
                .build();
    }
}
