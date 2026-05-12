package com.clinic.domain.mapper;

import com.clinic.domain.dto.MedicineRequest;
import com.clinic.domain.dto.MedicineResponse;
import com.clinic.domain.entity.Medicine;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi") // Sử dụng CDI để Quarkus có thể Inject được
public interface MedicineMapper {

    // Chuyển từ Request sang Entity để lưu DB
    // MapStruct sẽ tự khớp các trường cùng tên như: name, unit
    Medicine toEntity(MedicineRequest request);

    // Chuyển từ Entity sang Response để trả về Client
    MedicineResponse toResponse(Medicine medicine);
}