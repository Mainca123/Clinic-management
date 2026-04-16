package com.clinic.config;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import jakarta.ws.rs.core.Application;

@OpenAPIDefinition(
        info = @Info(
                title = "Clinic Management API",
                version = "1.0.0",
                description = "Hệ thống quản lý phòng khám thông minh"
        )
)
public class OpenApiConfig extends Application {}