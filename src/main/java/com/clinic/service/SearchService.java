package com.clinic.service;

import com.clinic.domain.dto.AppointmentResponse;
import com.clinic.domain.dto.DepartmentResponse;
import com.clinic.domain.dto.DoctorResponse;
import com.clinic.domain.dto.SearchResponse;
import com.clinic.domain.dto.UserResponse;

import com.clinic.domain.entity.Appointment;
import com.clinic.domain.entity.Department;
import com.clinic.domain.entity.Doctor;
import com.clinic.domain.entity.User;

import com.clinic.repository.AppointmentRepository;
import com.clinic.repository.DepartmentRepository;
import com.clinic.repository.DoctorRepository;
import com.clinic.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class SearchService {

    private static final int LIMIT = 10;

    @Inject
    DoctorRepository doctorRepository;

    @Inject
    AppointmentRepository appointmentRepository;

    @Inject
    DepartmentRepository departmentRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    JsonWebToken jwt;

    public SearchResponse searchInfo(String keyword) {

        String role = getRole();
        Long userId = getCurrentUserId();

        return switch (role) {

            case "ADMIN" -> SearchResponse.builder()
                    .doctors(searchDoctors(keyword))
                    .departments(searchDepartments(keyword))
                    .patients(searchPatients(keyword, userId))
                    .appointments(empty())
                    .build();

            case "DOCTOR" -> SearchResponse.builder()
                    .doctors(empty())
                    .departments(empty())
                    .patients(searchPatients(keyword, userId))
                    .appointments(searchAppointmentsForDoctor(keyword, userId))
                    .build();

            case "PATIENT" -> SearchResponse.builder()
                    .doctors(searchDoctors(keyword))
                    .departments(searchDepartments(keyword))
                    .patients(empty())
                    .appointments(searchAppointmentsForPatient(keyword, userId))
                    .build();

            default -> throw new RuntimeException("Role invalid");
        };
    }

    // =========================
    // SEARCH METHODS
    // =========================

    private List<DoctorResponse> searchDoctors(String keyword) {

        return doctorRepository
                .searchDoctor(keyword, LIMIT)
                .stream()
                .map(this::mapDoctor)
                .toList();
    }

    private List<DepartmentResponse> searchDepartments(String keyword) {

        return departmentRepository
                .searchDepartment(keyword, LIMIT)
                .stream()
                .map(this::mapDepartment)
                .toList();
    }

    private List<UserResponse> searchPatients(String keyword, Long userId) {

        return userRepository
                .searchUserByFullName(keyword, userId, LIMIT)
                .stream()
                .map(this::mapUser)
                .toList();
    }

    private List<AppointmentResponse> searchAppointmentsForDoctor(
            String keyword,
            Long doctorId
    ) {

        return appointmentRepository
                .searchAppointmentByDoctor(keyword, doctorId, LIMIT)
                .stream()
                .map(this::mapAppointment)
                .toList();
    }

    private List<AppointmentResponse> searchAppointmentsForPatient(
            String keyword,
            Long patientId
    ) {

        return appointmentRepository
                .searchAppointmentByPatient(keyword, patientId, LIMIT)
                .stream()
                .map(this::mapAppointment)
                .toList();
    }

    // =========================
    // MAPPERS
    // =========================

    private DoctorResponse mapDoctor(Doctor doctor) {

        return DoctorResponse.builder()
                .id(doctor.id)
                .fullName(doctor.getUser().getFullName())
                .email(doctor.getUser().getEmail())
                .specialization(doctor.getSpecialization())
                .experienceYears(doctor.getExperienceYears())
                .build();
    }

    private DepartmentResponse mapDepartment(Department department) {

        return DepartmentResponse.builder()
                .id(department.id)
                .name(department.getName())
                .description(department.getDescription())
                .build();
    }

    private UserResponse mapUser(User user) {

        return UserResponse.builder()
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .email(user.getEmail())
                .dateOfBirth(user.getDateOfBirth())
                .build();
    }

    private AppointmentResponse mapAppointment(Appointment appointment) {

        return AppointmentResponse.builder()
                .id(appointment.id)
                .appointmentDate(appointment.getAppointmentDate())
                .status(appointment.getStatus())
                .build();
    }

    // =========================
    // HELPERS
    // =========================

    private String getRole() {

        return jwt.getGroups()
                .iterator()
                .next();
    }

    private Long getCurrentUserId() {

        return Long.valueOf(jwt.getSubject());
    }

    private <T> List<T> empty() {

        return Collections.emptyList();
    }
}