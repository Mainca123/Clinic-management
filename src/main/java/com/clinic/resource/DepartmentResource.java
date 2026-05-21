package com.clinic.resource;

import com.clinic.base.RestData;
import com.clinic.constant.RoleType;
import com.clinic.domain.dto.DepartmentRequest;
import com.clinic.service.DepartmentService;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/departments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Department", description = "Quản lý khoa")
@Authenticated
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


    @GET
    @Operation(
            summary = "Lấy phòng ban",
            description = "API dùng để lấy tất cả các phòng ban trong hệ thống"
    )
    @APIResponse(
            responseCode = "200",
            description = "Lấy thành công")
    public RestData<?> getAllDepartment(
            @QueryParam("page") @DefaultValue("0")
            int page){

        return RestData.success(
                departmentService.getAllDepartment(page)
        );
    }

    @PATCH
    @Path("/{id}/delete")
//    @RolesAllowed(RoleType.Constants.ADMIN)
    @RolesAllowed({"ADMIN","DOCTOR"}) // Chỉ Admin mới có quyền xóa khoa
    @Operation(
            summary = "Admin xóa khoa (Xóa mềm)",
            description = "API dùng để đổi trạng thái hoạt động của khoa thành đã xóa"
    )
    @APIResponse(
            responseCode = "200",
            description = "Xóa khoa thành công"
    )
    @APIResponse(
            responseCode = "403",
            description = "Không có quyền truy cập"
    )



    public RestData<String> deleteDepartment(
            @PathParam("id")
            @Parameter(description = "ID của khoa cần xóa", required = true)
            Long id
    ) {
        return RestData.success(
                departmentService.deleteDepartment(id)
        );
    }

    // Bổ sung vào class DepartmentResource.java

    @PATCH
    @Path("/{id}")
//    @RolesAllowed(RoleType.Constants.ADMIN) // Chỉ Admin mới có quyền chỉnh sửa thông tin khoa
    @RolesAllowed({"ADMIN","DOCTOR"})
    @Operation(
            summary = "Admin sửa thông tin khoa",
            description = "API dùng để cập nhật tên hoặc mô tả của một khoa theo ID"
    )
    @APIResponse(
            responseCode = "200",
            description = "Cập nhật thông tin khoa thành công"
    )
    @APIResponse(
            responseCode = "403",
            description = "Không có quyền truy cập"
    )
    public RestData<String> updateDepartment(
            @PathParam("id")
            @Parameter(description = "ID của khoa cần chỉnh sửa", required = true)
            Long id,

            @RequestBody(description = "Thông tin khoa cần cập nhật", required = true)
            DepartmentRequest departmentRequest
    ) {
        String result = departmentService.updateDepartment(id, departmentRequest);
        return RestData.success(result);
    }

}