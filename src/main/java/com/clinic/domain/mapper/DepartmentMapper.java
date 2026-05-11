package com.clinic.domain.mapper;


import com.clinic.domain.dto.DepartmentRequest;
import com.clinic.domain.entity.Department;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface DepartmentMapper {
    Department toDepartMent(DepartmentRequest request);
}
