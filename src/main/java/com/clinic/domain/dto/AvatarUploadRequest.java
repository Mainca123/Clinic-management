package com.clinic.domain.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.jboss.resteasy.reactive.multipart.FileUpload;

public class AvatarUploadRequest {

    @Schema(type = org.eclipse.microprofile.openapi.annotations.enums.SchemaType.STRING, format = "binary")
    public FileUpload file;
}