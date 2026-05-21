package com.clinic.resource;

import com.clinic.base.RestData;
import com.clinic.constant.RoleType;
import com.clinic.domain.dto.DoctorCreateRequest;
import com.clinic.domain.dto.DoctorUpdateRequest;
import com.clinic.service.DoctorService;

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
import org.jboss.resteasy.reactive.RestPath;

@Path("/doctors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Doctor", description = "Quản lý bác sĩ")
@Authenticated
public class DoctorResource {

    @Inject
    private DoctorService doctorService;

    @POST
    @RolesAllowed(RoleType.Constants.ADMIN)
    @Operation(
            summary = "Tạo bác sĩ",
            description = "API dùng để thêm mới bác sĩ vào hệ thống"
    )
    @APIResponse(
            responseCode = "200",
            description = "Tạo bác sĩ thành công"
    )
    @APIResponse(
            responseCode = "403",
            description = "Không có quyền truy cập"
    )
    public RestData<?> createDoctor(
            @RequestBody(
                    description = "Thông tin bác sĩ cần tạo",
                    required = true
            )
            DoctorCreateRequest request){

        return RestData.success(
                doctorService.createDoctor(request)
        );
    }

    @PATCH
    @RolesAllowed(RoleType.Constants.ADMIN)
    @Operation(
            summary = "Cập nhật bác sĩ",
            description = "API dùng để cập nhật thông tin bác sĩ"
    )
    @APIResponse(
            responseCode = "200",
            description = "Cập nhật bác sĩ thành công"
    )
    @APIResponse(
            responseCode = "404",
            description = "Không tìm thấy bác sĩ"
    )
    @APIResponse(
            responseCode = "403",
            description = "Không có quyền truy cập"
    )
    public RestData<?> updateDoctor(
            @RequestBody(
                    description = "Thông tin bác sĩ cần cập nhật",
                    required = true
            )
            DoctorUpdateRequest doctorUpdateRequest){

        return RestData.success(
                doctorService.updateDoctor(doctorUpdateRequest)
        );
    }


    @GET
    @Operation(
            summary = "Lấy tất cả các bác sĩ",
            description = "API dùng để lấy tất cả các bác sĩ"
    )
    @APIResponse(
            responseCode = "200",
            description = "Lấy thành công"
    )
    @APIResponse(
            responseCode = "404",
            description = "Không tìm thấy bác sĩ")
    public RestData<?> getAllDoctor(@QueryParam("page") @DefaultValue("0") int page) {
        return RestData.success(doctorService.getAllDoctor(page));
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed(RoleType.Constants.ADMIN) // Chỉ Admin mới có quyền xóa bác sĩ
    @Operation(
            summary = "Admin xóa bác sĩ",
            description = "API dùng để thay đổi trạng thái tài khoản của bác sĩ thành đã xóa (Xóa mềm)"
    )
    @APIResponse(
            responseCode = "200",
            description = "Xóa thông tin bác sĩ thành công"
    )
    @APIResponse(
            responseCode = "403",
            description = "Không có quyền truy cập"
    )


    public RestData<String> deleteDoctor(
            @PathParam("id")
            @Parameter(description = "ID của bác sĩ cần xóa", required = true)
            Long id
    ) {
        String result = doctorService.deleteDoctor(id);
        return RestData.success(result);

    }
}
