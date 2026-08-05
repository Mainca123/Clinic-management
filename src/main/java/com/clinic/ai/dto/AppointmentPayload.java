package com.clinic.ai.dto;

import com.clinic.domain.dto.AppointmentResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentPayload {
    private String action;
    private AppointmentResponse appointment;
    private List<AppointmentResponse> appointments;
    private boolean requiresConfirmation;
    private Instant confirmationExpiresAt;
}
