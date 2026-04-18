package com.clinic.repository;

import com.clinic.domain.entity.PrescriptionDetail;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class PrescriptionDetailRepository implements PanacheRepository<PrescriptionDetail> {
}
