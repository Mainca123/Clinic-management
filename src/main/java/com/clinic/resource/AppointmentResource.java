package com.clinic.resource;

import com.clinic.base.RestData;
import com.clinic.constant.RoleType;
import com.clinic.domain.dto.AppointmentRequest;
import com.clinic.domain.dto.AppointmentResponse;
import com.clinic.domain.dto.StatusRequest;
import com.clinic.service.AppointmentService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("appointments") // Đường dẫn API chuẩn RESTful
@Consumes(MediaType.APPLICATION_JSON) // Nhận dữ liệu JSON
@Produces(MediaType.APPLICATION_JSON) // Trả về dữ liệu JSON
@Tag(name = "Quản lý Lịch hẹn", description = "Các API liên quan đến lịch khám bệnh")
@Authenticated
public class AppointmentResource {

    @Inject
    AppointmentService appointmentService;
    @Inject
    JsonWebToken jwt; // Dùng để lấy ID người dùng từ Token

    @POST
//    @RolesAllowed("PATIENT") // Chỉ bệnh nhân mới được tạo lịch
    @Operation(
            summary = "Tạo mới một lịch hẹn",
            description = "Bệnh nhân gửi yêu cầu đặt lịch hẹn với bác sĩ dựa trên khung giờ đã chọn."
    )
    public RestData<?> create(AppointmentRequest request) {
        // Return trực tiếp kết quả từ Service, Quarkus tự bọc lại thành JSON
        return RestData.success(appointmentService.createAppointment(request));
    }

    @GET
    @RolesAllowed({"PATIENT", "DOCTOR"}) // Cả 2 quyền đều có thể xem danh sách
    @Operation(
            summary = "Danh sách lịch hẹn",
            description = "Lấy danh sách lịch hẹn có phân trang, tự động lọc theo người dùng đang đăng nhập."
    )
    public RestData<?> list(
            @Context SecurityContext sec,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        String subject = jwt.getSubject();
        // Kiểm tra nếu Token hợp lệ nhưng không có ID (subject)
        if (subject == null || subject.isEmpty()) {
            return RestData.error("Token không chứa thông tin định danh (subject null)");
        }
        try {
        // Lấy ID người dùng từ token (thông tin này được nạp khi đăng nhập thành công)
        Long currentUserId = Long.parseLong(jwt.getSubject());

        // Xác định vai trò để Service lọc dữ liệu đúng bảng
        String role = sec.isUserInRole("DOCTOR") ? "DOCTOR" : "PATIENT";

        return RestData.success(appointmentService.getAppointments(currentUserId, role, page, size));
        } catch (NumberFormatException e) {
            return RestData.error("ID người dùng trong Token không đúng định dạng số");
        }
    }

    @PATCH
    @Path("/{id}/status")
    @RolesAllowed({"DOCTOR", "PATIENT"})
    @Operation(
            summary = "Xác nhận hoặc hủy lịch hẹn",
            description = "Bác sĩ dùng để xác nhận lịch, Bệnh nhân hoặc Bác sĩ dùng để hủy lịch."
    )
    public RestData<?> updateStatus(
            @PathParam("id") Long id,
            StatusRequest request) {

        return RestData.success(appointmentService.updateStatus(id, request.getStatus()));
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"DOCTOR", "PATIENT", "ADMIN"}) // Cho phép các role liên quan xem chi tiết
    @Operation(
            summary = "Lấy chi tiết lịch hẹn",
            description = "Cung cấp ID của lịch hẹn để lấy thông tin chi tiết đầy đủ."
    )
    public RestData<?> getAppointment(@PathParam("id") Long id) {
        return RestData.success(appointmentService.getById(id));
    }


    @PATCH
    @Path("/{id}/status/delete") // Thêm hậu tố rõ ràng để tránh xung đột endpoint với API update status cũ
    @RolesAllowed({"ADMIN", "DOCTOR", "PATIENT"}) // Các quyền được phép hủy/xóa lịch hẹn
    @Operation(
            summary = "Xóa/Hủy trạng thái lịch hẹn",
            description = "Thay đổi trạng thái xóa mềm (isDeleted = true) và cập nhật thời gian xóa của lịch hẹn theo ID"
    )
    public RestData<String> deleteStatus(@PathParam("id") Long id) {
        appointmentService.deleteAppointmentStatus(id);
        return RestData.success("Xóa/Hủy lịch hẹn thành công");
    }
}