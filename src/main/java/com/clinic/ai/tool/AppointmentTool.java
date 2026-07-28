package com.clinic.ai.tool;

import com.clinic.ai.dto.ChatResponse;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AppointmentTool {

    public ChatResponse bookAppointment(String doctor,
                                        String date,
                                        String time) {

        // TODO:
        // Kiểm tra bác sĩ
        // Kiểm tra lịch trống
        // Lưu DB

        return new ChatResponse(
                "Đặt lịch thành công với bác sĩ " + doctor
                        + " vào " + date
                        + " lúc " + time + ".",
                "BOOK_APPOINTMENT",
                true,
                null,
                null
        );
    }
}