package com.clinic.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;

@ApplicationScoped
public class FileUploadService {

    @Inject
    Cloudinary cloudinary;

    public String upload(byte[] fileBytes) {
        try {
            Map<?, ?> result = cloudinary.uploader()
                    .upload(fileBytes, ObjectUtils.emptyMap());

            return result.get("secure_url").toString();

        } catch (Exception e) {
            throw new RuntimeException("Upload to Cloudinary failed", e);
        }
    }
}