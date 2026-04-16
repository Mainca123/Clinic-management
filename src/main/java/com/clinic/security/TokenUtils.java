package com.clinic.security;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;

@ApplicationScoped
public class TokenUtils {

    public String generateToken(String username, String role) {
        // Quarkus SmallRye sẽ tự tìm file privateKey.pem trong resources để ký
        return Jwt.issuer("https://clinic-management.com")
                .upn(username)
                .groups(role)
                .expiresIn(Duration.ofHours(8))
                .sign();
    }
}