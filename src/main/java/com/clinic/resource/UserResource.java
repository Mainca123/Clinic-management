package com.clinic.resource;


import com.clinic.base.RestData;
import com.clinic.domain.dto.PasswordRequest;
import com.clinic.domain.dto.UserListResponse;
import com.clinic.domain.dto.UserUpdateRequest;
import com.clinic.service.UserService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;


import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@Tag(name = "User", description = "Quản lý thông tin người dùng")
public class UserResource {

    @Inject
    JsonWebToken jwt;

    @Inject
    UserService userService;

    @GET
    @Path("/me")
    @Operation(
            summary = "Lấy thông tin người dùng hiện tại",
            description = "API dùng để lấy thông tin cá nhân từ JWT token"
    )
    @APIResponse(
            responseCode = "200",
            description = "Lấy thông tin người dùng thành công"
    )
    public RestData<?> getCurrentUser() {
        String identifier = getIdentifierFromToken();
        return RestData.success(userService.getUserDetail(identifier));

    }

    @PATCH
    @Operation(
            summary = "Cập nhật thông tin cá nhân",
            description = "API dùng để cập nhật thông tin của người dùng hiện tại"
    )
    @APIResponse(
            responseCode = "200",
            description = "Cập nhật thông tin thành công"
    )
    public RestData<?> updateUser(
            @Valid
            @RequestBody(
                    description = "Thông tin người dùng cần cập nhật",
                    required = true
            )
            UserUpdateRequest request) {

        String identifier = getIdentifierFromToken();
        return RestData.success(userService.updateCurrentUser(identifier, request));
    }


    @PATCH
    @Path("/me/avatar")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(
            summary = "Upload ảnh đại diện",
            description = "API dùng để tải ảnh đại diện cho người dùng hiện tại"
    )
    @APIResponse(
            responseCode = "200",
            description = "Upload avatar thành công"
    )
    public RestData<?> uploadAvatar(

            @Parameter(
                    description = "File ảnh đại diện cần upload"
            )
            @RestForm("file") FileUpload file
    ) {
        String identifier = getIdentifierFromToken();
        return RestData.success(userService.uploadAvatar(identifier, file));
    }


    @PATCH
    @Path("/password")
    @Operation(
            summary = "Đổi mật khẩu",
            description = "API dùng để thay đổi mật khẩu của người dùng hiện tại"
    )
    @APIResponse(
            responseCode = "200",
            description = "Đổi mật khẩu thành công"
    )
    @APIResponse(
            responseCode = "400",
            description = "Mật khẩu cũ không chính xác"
    )
    public RestData<?> changePassword(

            @Valid
            @RequestBody(
                    description = "Thông tin đổi mật khẩu",
                    required = true
            )
            PasswordRequest request) {

        String identifier = getIdentifierFromToken();
        return RestData.success(userService.changePassword(identifier, request));
    }


    private String getIdentifierFromToken() {
        return jwt.getClaim("upn");
    }

    // Sửa lại API GET danh sách người dùng tại UserResource.java

    @GET
    @RolesAllowed({"ADMIN", "DOCTOR"}) // Chỉ cho phép Admin và Doctor gọi API này
    @Operation(summary = "Lấy danh sách người dùng/bệnh nhân có phân trang")
    public RestData<UserListResponse> listUsers(
            @Context SecurityContext sec,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        // 1. Lấy thông tin định danh ID và Role từ Token người dùng đang đăng nhập
        Long currentUserId = Long.parseLong(jwt.getSubject());
        String role = sec.isUserInRole("ADMIN") ? "ADMIN" : "DOCTOR";

        // 2. Gọi sang Service để xử lý phân quyền lấy dữ liệu
        UserListResponse response = userService.getUsers(currentUserId, role, page, size);

        return RestData.success(response);
    }

    // Bổ sung vào class UserResource.java

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"ADMIN","DOCTOR"}) // Chỉ Admin mới có quyền khóa tài khoản người dùng
    @Operation(
            summary = "Admin khóa người dùng (Xóa mềm)",
            description = "API dùng để chuyển trạng thái tài khoản người dùng sang bị xóa và lưu ngày xóa"
    )
    @APIResponse(
            responseCode = "200",
            description = "Khóa người dùng thành công"
    )
    public RestData<String> deleteUser(
            @PathParam("id")
            @Parameter(description = "ID của người dùng cần khóa", required = true)
            Long id
    ) {
        String result = userService.deleteUser(id);
        return RestData.success(result);
    }

    // Thêm vào file UserResource.java

    @GET
    @Path("/patients-by-doctor")
    @RolesAllowed("ADMIN") // Chỉ Admin mới có quyền chọn bác sĩ để xem danh sách này
    @Operation(
            summary = "Admin xem danh sách bệnh nhân của một bác sĩ",
            description = "API giúp Admin truyền ID bác sĩ từ giao diện quản lý để xem toàn bộ bệnh nhân của bác sĩ đó."
    )
    public RestData<UserListResponse> getPatientsByDoctorForAdmin(
            @QueryParam("doctorId") Long doctorId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        // Kiểm tra nếu Admin chưa chọn bác sĩ (truyền thiếu param)
        if (doctorId == null) {
            return RestData.error("Vui lòng cung cấp ID bác sĩ (doctorId không được để trống)");
        }

        // Gọi sang Service xử lý dữ liệu
        UserListResponse response = userService.getPatientsForAdmin(doctorId, page, size);

        return RestData.success(response);
    }
}