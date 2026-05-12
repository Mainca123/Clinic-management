package com.clinic.resource;

import com.clinic.base.RestData;
import com.clinic.constant.RoleType;
import com.clinic.domain.dto.DepartmentRequest;
import com.clinic.service.DepartmentService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/departments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Department", description = "Quản lý khoa")
public class DepartmentResource {

    @Inject
    private DepartmentService departmentService;

    @POST
    @RolesAllowed(RoleType.Constants.ADMIN)
    @Operation(
            summary = "Tạo phòng ban",
            description = "API dùng để tạo mới khoa hoặc phòng ban trong hệ thống"
    )
    @APIResponse(
            responseCode = "200",
            description = "Tạo phòng ban thành công"
    )
    @APIResponse(
            responseCode = "403",
            description = "Không có quyền truy cập"
    )
    public RestData<?> createDepartment(
            @RequestBody(
                    description = "Thông tin phòng ban cần tạo",
                    required = true
            )
            DepartmentRequest departmentRequest){

        return RestData.success(
                departmentService.createDepartment(departmentRequest)
        );
    }
}