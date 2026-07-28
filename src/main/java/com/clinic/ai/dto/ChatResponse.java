package com.clinic.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {
    // AI nói gì với người dùng
    private String message;

    // Tool nào đã được thực thi
    private String tool;

    // Thành công hay thất bại
    private boolean success;

    // Dữ liệu có cấu trúc
    private Object payload;

    // Metadata nếu cần
    private Map<String, Object> metadata;
}
