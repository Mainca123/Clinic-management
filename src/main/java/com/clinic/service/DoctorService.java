package com.clinic.service;

import com.clinic.constant.RoleType;
import com.clinic.domain.dto.DoctorCreateRequest;
import com.clinic.domain.dto.DoctorListResponse;
import com.clinic.domain.dto.DoctorResponse;
import com.clinic.domain.dto.DoctorUpdateRequest;
import com.clinic.domain.entity.Department;
import com.clinic.domain.entity.Doctor;
import com.clinic.domain.entity.User;
import com.clinic.domain.mapper.UserMapper;
import com.clinic.repository.DepartmentRepository;
import com.clinic.repository.DoctorRepository;
import com.clinic.repository.UserRepository;
import com.clinic.security.TokenUtils;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import io.quarkus.panache.common.Page;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class DoctorService {

    @Inject
    private DepartmentRepository departmentRepository;

    @Inject
    private DoctorRepository doctorRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserMapper userMapper;

    @Inject
    private EmailService emailService;

    @Inject
    TokenUtils tokenUtils;

    @Transactional
    public String createDoctor(DoctorCreateRequest doctorCreateRequest){
        User user = userMapper.toUser(doctorCreateRequest);
        user.setPassword(BcryptUtil.bcryptHash(doctorCreateRequest.getPassword()));
        user.setRole(RoleType.DOCTOR);
        user.setTokenVerified(tokenUtils.generateVerifyToken(user.getEmail()));
        user.setIsVerified(false);
        userRepository.persist(user);

        Department department = departmentRepository.findDepartmentById(doctorCreateRequest.getDepartmentId()).orElseThrow(()
        -> new RuntimeException("department.not.found"));

        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setDepartment(department);
        doctor.setExperienceYears(doctorCreateRequest.getExperienceYears());
        doctor.setSpecialization(doctorCreateRequest.getSpecialization());
        doctorRepository.persist(doctor);

        emailService.sendVerificationEmail(user.getEmail(), user.getTokenVerified());
        return "SUCCESS";
    }

    @Transactional
    public String updateDoctor(DoctorUpdateRequest request){
        Doctor doctor = doctorRepository.findByUserId(request.getUserId()).orElseThrow(()
        -> new RuntimeException("not.found.doctor"));

        Department department = departmentRepository.findDepartmentById(request.getDepartmentId()).orElseThrow(()
                -> new RuntimeException("not.found.department" + request.getDepartmentId()));

        doctor.setDepartment(department);
        doctor.setSpecialization(request.getSpecialization());
        doctor.setExperienceYears(request.getExperienceYears());

        doctorRepository.persist(doctor);
        return "SUCCESS";
    }


    @Transactional
    public DoctorListResponse getAllDoctor(int page){

        var doctorQuery = doctorRepository.findAll();

        doctorQuery.page(Page.of(page, 20));

        List<DoctorResponse> doctorResponses = doctorQuery.list()
                .stream()
                .map(doctor -> DoctorResponse.builder()
                        .id(doctor.id)
                        .fullName(doctor.getUser().getFullName())
                        .email(doctor.getUser().getEmail())
                        .departmentName(doctor.getDepartment().getName())
                        .specialization(doctor.getSpecialization())
                        .experienceYears(doctor.getExperienceYears())
                        .build())
                .toList();

        return DoctorListResponse.builder()
                .doctorList(doctorResponses)
                .totalItems(doctorQuery.count())
                .totalPages(doctorQuery.pageCount())
                .build();
    }


    // Thêm vào class DoctorService.java

    @Transactional
    public String deleteDoctor(Long id) {
        // 1. Tìm bác sĩ theo ID và chưa bị xóa
        Doctor doctor = doctorRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ với ID: " + id));

        // 2. Chuyển đổi trạng thái tài khoản thành đã xóa (Xóa mềm)
        doctor.setIsDeleted(true);

        // Nếu trong hệ thống của bạn có trường ngày xóa (như bảng Users), bạn bổ sung thêm dòng dưới:
        // doctor.setDeletedAt(LocalDateTime.now());

        // 3. Cập nhật lại vào Database
        doctorRepository.persist(doctor);

        // Trả về chuỗi thông báo thành công theo đúng tài liệu thiết kế
        return "Thông báo thành công"; // Hoặc "OK" tùy bạn chuẩn hóa text trả về
    }

}
