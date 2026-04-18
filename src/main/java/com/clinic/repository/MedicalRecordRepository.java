package com.clinic.repository;

import com.clinic.domain.entity.MedicalRecord;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.Optional;

@ApplicationScoped
public class MedicalRecordRepository implements PanacheRepository<MedicalRecord>{
}
