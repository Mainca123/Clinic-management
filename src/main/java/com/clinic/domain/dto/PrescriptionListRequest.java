package com.clinic.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.util.List;

@Getter
@Setter
public class PrescriptionListRequest {

    @NotEmpty(message = "Đơn thuốc phải có ít nhất một loại thuốc")
    @Valid // Đảm bảo kiểm tra validate @Min, @NotBlank cho từng item bên trong
    @Schema(description = "Danh sách các thuốc được kê")
    private List<PrescriptionItemRequest> medicines;
}