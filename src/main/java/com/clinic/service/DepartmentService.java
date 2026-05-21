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
    public DepartmentListResponse getAllDepartment(int page) {

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

    @Transactional
    public String deleteDepartment(Long id) {
        // 1. Tìm khoa theo ID từ Repository, nếu không thấy thì ném lỗi
        Department department = departmentRepository.findByIdOptional(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa/phòng ban với ID: " + id));

        // 2. Kiểm tra xem khoa này đã bị xóa mềm từ trước chưa
        if (Boolean.TRUE.equals(department.getIsDeleted())) {
            throw new RuntimeException("Khoa này đã được xóa hoặc khóa từ trước.");
        }

        // 3. Thực hiện cập nhật trạng thái xóa mềm
        department.setIsDeleted(true);
        department.setDeletedAt(java.time.Instant.now());

        // Hibernate tự động đồng bộ trạng thái thực thể xuống Database khi kết thúc Transaction
        departmentRepository.persist(department);

        return "SUCCESS"; // Trả về thông báo thành công tương tự API tạo
    }


    @Transactional
    public String updateDepartment(Long id, DepartmentRequest request) {
        // 1. Tìm phòng ban theo ID, đảm bảo phòng ban tồn tại và chưa bị xóa mềm
        Department department = departmentRepository.findByIdOptional(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa/phòng ban với ID: " + id));

        if (Boolean.TRUE.equals(department.getIsDeleted())) {
            throw new RuntimeException("Không thể chỉnh sửa khoa đã bị xóa/khóa.");
        }

        // 2. Kiểm tra và cập nhật các thông tin thay đổi từ Request (tránh ghi đè null)
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            department.setName(request.getName().trim());
        }

        if (request.getDescription() != null) {
            department.setDescription(request.getDescription().trim());
        }

        // 3. Lưu lại vào DB (Hibernate quản lý thực thể sẽ tự động Sync khi hết Transaction)
        departmentRepository.persist(department);

        return "OK"; // Trả về thông báo "OK" đúng theo tài liệu thiết kế

    }
}
