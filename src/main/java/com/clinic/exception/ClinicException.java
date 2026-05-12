package com.clinic.exception;

import jakarta.ws.rs.core.Response;
import lombok.Getter;

@Getter
public class ClinicException extends RuntimeException {
    private final Response.Status status;

    public ClinicException(String message, Response.Status status) {
        super(message);
        this.status = status;
    }
}
