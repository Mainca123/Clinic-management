package com.clinic.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
public class PrescriptionItemRequest {

    @NotNull(message = "ID thuốc không được để trống")
    @Schema(description = "ID của thuốc chọn từ danh mục", example = "1")
    private Long medicineId;

    @NotNull(message = "Số lượng thuốc không được để trống")
    @Min(value = 1, message = "Số lượng thuốc phải lớn hơn hoặc bằng 1")
    @Schema(description = "Số lượng cấp phát", example = "10")
    private Integer quantity;

    @NotBlank(message = "Liều dùng không được để trống")
    @Schema(description = "Cách dùng, liều lượng uống", example = "Ngày uống 2 lần, mỗi lần 1 viên sau ăn")
    private String dosage;
}