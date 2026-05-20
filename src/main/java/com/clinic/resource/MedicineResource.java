package com.clinic.resource;

import com.clinic.base.RestData;
import com.clinic.domain.dto.MedicineRequest;
import com.clinic.domain.dto.MedicineResponse;
import com.clinic.service.MedicineService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("medicines")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Medicine", description = "Quản lý danh mục thuốc") // Ghi chú cho Swagger
public class MedicineResource {

    @Inject
    MedicineService medicineService;

    @POST
    @RolesAllowed({"ADMIN"}) // Chỉ cho phép Admin
    @Operation(summary = "Thêm thuốc mới", description = "Tạo một loại thuốc mới vào danh mục")
    public RestData<MedicineResponse> create(@Valid MedicineRequest request) {
        // @Valid dùng để kích hoạt kiểm tra @NotBlank, @Size trong DTO
        MedicineResponse response = medicineService.create(request);
        return RestData.success(response);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "DOCTOR", "PATIENT"}) // Tất cả các role đều có quyền xem thông tin thuốc
    @Operation(summary = "Xem chi tiết thuốc", description = "Cung cấp ID để xem thông tin chi tiết của thuốc")
    public RestData<MedicineResponse> getById(@PathParam("id") Long id) {
        // Gọi service để lấy dữ liệu và đóng gói vào RestData chuẩn
        MedicineResponse response = medicineService.getById(id);
        return RestData.success(response);
    }

    @GET
    @RolesAllowed({"ADMIN", "DOCTOR", "PATIENT"}) // Cho phép mọi user xem danh mục thuốc
    @Operation(summary = "Lấy danh sách thuốc", description = "Trả về toàn bộ danh mục thuốc hiện có trong hệ thống")
    public RestData<List<MedicineResponse>> getAll() {
        List<MedicineResponse> response = medicineService.getAll();
        return RestData.success(response);
    }

    // Trong MedicineResource.java
    @PATCH
    @Path("/{id}")
    @RolesAllowed({"ADMIN"}) // Thường chỉ Admin mới được quyền sửa danh mục thuốc
    @Operation(summary = "Sửa thông tin thuốc", description = "Cập nhật tên hoặc đơn vị tính của thuốc theo ID")
    public RestData<MedicineResponse> update(
            @PathParam("id") Long id,
            @Valid MedicineRequest request) {

        MedicineResponse response = medicineService.update(id, request);
        return RestData.success(response);
    }

    // Trong MedicineResource.java
    @PATCH
    @Path("/{id}/delete") // Thêm /delete để phân biệt với API update thông tin
    @RolesAllowed({"ADMIN"}) // Chỉ Admin mới được quyền xóa danh mục
    @Operation(summary = "Xóa thuốc", description = "Đánh dấu thuốc đã bị xóa trong hệ thống")
    public RestData<String> delete(@PathParam("id") Long id) {
        medicineService.delete(id);
        return RestData.success("Xóa thuốc thành công");
    }
}
