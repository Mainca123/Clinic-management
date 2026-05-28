package com.clinic.resource;

import com.clinic.base.RestData;
import com.clinic.constant.RoleType;
import com.clinic.domain.dto.MedicalRecordRequest;
import com.clinic.domain.dto.MedicalRecordResponse;
import com.clinic.domain.dto.PrescriptionDetailResponse;
import com.clinic.domain.dto.PrescriptionListRequest;
import com.clinic.exception.ClinicException;
import com.clinic.service.MedicalRecordService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("medical-records")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Medical Record", description = "Quản lý hồ sơ bệnh án và đơn thuốc")
public class MedicalRecordResource {

    @Inject
    MedicalRecordService medicalRecordService;

    @POST
    @RolesAllowed({"ADMIN", "DOCTOR"}) // Chỉ Admin hoặc Bác sĩ mới có quyền lập hồ sơ bệnh án
    @Operation(summary = "Lập bệnh án mới", description = "Tạo bệnh án kèm theo đơn thuốc chi tiết dựa trên ID lịch hẹn")
    public RestData<MedicalRecordResponse> createRecord(@Valid MedicalRecordRequest request) {
        // Chạy qua xử lý nghiệp vụ tại service
        MedicalRecordResponse response = medicalRecordService.createRecord(request);
        // Trả dữ liệu chuẩn RestData thành công về Client
        return RestData.success(response);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "DOCTOR", "STAFF"}) // Thêm quyền STAFF (Nhân viên/Lễ tân) hoặc PATIENT tùy bạn nếu họ cần xem đơn thuốc để phát thuốc
    @Operation(summary = "Xem chi tiết bệnh án", description = "Lấy đầy đủ thông tin bệnh án và danh sách đơn thuốc chi tiết bằng ID bệnh án")
    public RestData<MedicalRecordResponse> getRecordById(@jakarta.ws.rs.PathParam("id") Long id) {
        // Gọi xuống service xử lý và đóng gói bằng chuẩn RestData thành công
        MedicalRecordResponse response = medicalRecordService.getRecordById(id);
        return RestData.success(response);
    }

    // Bổ sung endpoint này vào trong MedicalRecordResource.java

    @POST
    @Path("/{id}/prescriptions")
    @RolesAllowed({"DOCTOR", "ADMIN"}) // Chỉ bác sĩ hoặc admin mới được bổ sung đơn thuốc
    @Operation(summary = "Bổ sung đơn thuốc vào bệnh án", description = "Lập thêm một hoặc nhiều đơn thuốc bổ sung cho bệnh nhân dựa trên ID bệnh án")
    public RestData<List<PrescriptionDetailResponse>> addPrescription(
            @PathParam("id") Long medicalRecordId,
            @Valid PrescriptionListRequest request) {

        List<PrescriptionDetailResponse> response = medicalRecordService.addPrescriptionToRecord(medicalRecordId, request);
        return RestData.success(response);
    }

    @GET
    @Path("/{id}/prescriptions")
    @RolesAllowed({"ADMIN", "DOCTOR", "STAFF"}) // Cho phép các quyền liên quan được phép xem đơn thuốc
    @Operation(summary = "Lấy đơn thuốc theo bệnh án", description = "Lấy danh sách chi tiết các thuốc, liều lượng, cách dùng của một bệnh án cụ thể")
    public RestData<List<PrescriptionDetailResponse>> getPrescriptionsByRecord(@PathParam("id") Long medicalRecordId) {

        List<PrescriptionDetailResponse> response = medicalRecordService.getPrescriptionByMedicalRecordId(medicalRecordId);
        return RestData.success(response);
    }

    @GET
    @RolesAllowed({"ADMIN", "DOCTOR", RoleType.Constants.PATIENT}) // Bác sĩ, admin và nhân viên lễ tân đều có quyền xem lịch sử bệnh án
    @Operation(summary = "Lịch sử bệnh án theo người dùng", description = "Lấy danh sách toàn bộ bệnh án chi tiết của một bệnh nhân theo ID")
    public RestData<List<MedicalRecordResponse>> getRecordsByPatient(@QueryParam("patientId") Long patientId) {

        // Nếu client không truyền patientId, có thể ném lỗi hoặc trả về danh sách trống tùy nghiệp vụ
        if (patientId == null) {
            throw new ClinicException("ID bệnh nhân không được để trống", Response.Status.BAD_REQUEST);
        }

        List<MedicalRecordResponse> response = medicalRecordService.getRecordsByPatientId(patientId);
        return RestData.success(response);
    }
}