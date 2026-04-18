package com.clinic.domain.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "medicines")
@Getter
@Setter
public class Medicine extends PanacheEntity {

    @Column(nullable = false, length = 150, unique = true)
    private String name;

    @Column(nullable = false, length = 50)
    private String unit;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}