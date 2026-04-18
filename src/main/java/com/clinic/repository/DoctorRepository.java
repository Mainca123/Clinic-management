package com.clinic.repository;
import com.clinic.domain.entity.Doctor;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class DoctorRepository implements PanacheRepository<Doctor> {
}
