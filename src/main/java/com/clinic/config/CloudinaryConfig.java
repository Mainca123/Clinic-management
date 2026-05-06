package com.clinic.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class CloudinaryConfig {

    @ConfigProperty(name = "cloudinary.cloud-name")
    String cloudName;

    @ConfigProperty(name = "cloudinary.api-key")
    String apiKey;

    @ConfigProperty(name = "cloudinary.api-secret")
    String apiSecret;

    @Produces
    @ApplicationScoped
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }
}