package com.clinic.ai.assistant.handler;

import com.clinic.ai.dto.ChatResponse;
import com.clinic.ai.dto.IntentResult;
import com.clinic.ai.tool.AppointmentTool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;

@ApplicationScoped
public class BookAppointmentHandler {

    @Inject
    AppointmentTool appointmentTool;

    public ChatResponse handle(IntentResult result) {

        Map<String, Object> params = result.getParams();

        if (params == null) {
            return new ChatResponse(
                    "Bạn vui lòng cung cấp đầy đủ thông tin đặt lịch.",
                    "BOOK_APPOINTMENT",
                    false,
                    null,
                    null
            );
        }

        String doctor = getString(params, "doctor");
        String date = getString(params, "date");
        String time = getString(params, "time");

        if (doctor == null || date == null || time == null) {
            return new ChatResponse(
                    "Bạn vui lòng cho biết bác sĩ, ngày khám và giờ khám.",
                    "BOOK_APPOINTMENT",
                    false,
                    null,
                    null
            );
        }

        return appointmentTool.bookAppointment(
                doctor,
                date,
                time
        );
    }

    private String getString(Map<String, Object> params, String key) {
        Object value = params.get(key);
        return value == null ? null : value.toString().trim();
    }
}