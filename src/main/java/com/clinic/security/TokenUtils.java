package com.clinic.security;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;

@ApplicationScoped
public class TokenUtils {

    public String generateToken(String username, String role) {
        return Jwt.issuer("https://clinic-management.com")
                .upn(username)
                .groups(role)
                .expiresIn(Duration.ofHours(8))
                .sign();
    }

    public String generateVerifyToken(String email) {
        return Jwt.issuer("https://clinic-management.com")
                .upn(email)
                .claim("type", "verify")
                .expiresIn(Duration.ofMinutes(15))
                .sign();
    }
}