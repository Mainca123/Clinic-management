package com.clinic.domain.mapper;

import com.clinic.domain.dto.AppointmentRequest;
import com.clinic.domain.dto.AppointmentResponse;
import com.clinic.domain.entity.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi") // "cdi" giúp Quarkus có thể @Inject Mapper này
public interface AppointmentMapper {

    // Map từ Request sang Entity
    // Vì DTO có patientId (Long) còn Entity cần đối tượng User, ta tạm thời bỏ qua (ignore) để xử lý trong Service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "patient", ignore = true) // Sẽ set trong Service
    @Mapping(target = "doctor", ignore = true)  // Sẽ set trong Service
    @Mapping(target = "status", ignore = true)  // Sẽ set mặc định là PENDING trong Service
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Appointment toEntity(AppointmentRequest request);

    // Map từ Entity sang Response
    @Mapping(source = "patient.fullName", target = "patientName")
    @Mapping(source = "doctor.user.fullName", target = "doctorName")
    AppointmentResponse toResponse(Appointment entity);
}