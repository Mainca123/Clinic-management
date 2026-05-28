package com.clinic.repository;

import com.clinic.domain.entity.Appointment;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AppointmentRepository implements PanacheRepository<Appointment> {
    /**
     * Kiểm tra bác sĩ có bận hay không
     */
    public boolean isDoctorBusy(Long doctorId, LocalDate date, LocalTime time) {

        return count("""
                doctor.id = ?1
                and appointmentDate = ?2
                and startTime = ?3
                and isDeleted = false
                """,
                doctorId,
                date,
                time
        ) > 0;
    }

    /**
     * Phân trang danh sách lịch hẹn
     */
    public PanacheQuery<Appointment> findByUserId(
            Long userId,
            String role,
            Page page
    ) {

        if ("DOCTOR".equals(role)) {

            return find("""
                    doctor.user.id = ?1
                    and isDeleted = false
                    """,
                    userId
            ).page(page);
        }

        return find("""
                patient.user.id = ?1
                and isDeleted = false
                """,
                userId
        ).page(page);
    }

    /**
     * Tìm appointment chưa bị xóa
     */
    public Optional<Appointment> findByIdAndNotDeleted(Long id) {

        return find("""
                id = ?1
                and isDeleted = false
                """,
                id
        ).firstResultOptional();
    }

    /**
     * Doctor tìm kiếm lịch hẹn của mình
     */
    public List<Appointment> searchAppointmentByDoctor(
            String keyword,
            Long doctorUserId,
            int SEARCH_LIMIT
    ) {

        if (keyword == null || keyword.trim().length() < 2) {
            return Collections.emptyList();
        }

        String search = "%" + keyword.trim().toLowerCase() + "%";

        return find("""
                select a
                from Appointment a
                where a.doctor.user.id = ?1
                and (
                    lower(a.patient.user.username) like ?2
                    or lower(a.status) like ?2
                )
                and a.isDeleted = false
                order by a.createdAt desc
                """,
                doctorUserId,
                search
        )
                .page(Page.ofSize(SEARCH_LIMIT))
                .list();
    }

    /**
     * Patient tìm kiếm lịch hẹn của mình
     */
    public List<Appointment> searchAppointmentByPatient(
            String keyword,
            Long patientUserId,
            int SEARCH_LIMIT
    ) {

        if (keyword == null || keyword.trim().length() < 2) {
            return Collections.emptyList();
        }

        String search = "%" + keyword.trim().toLowerCase() + "%";

        return find("""
            select a
            from Appointment a
            where a.patient.id = ?1
            and (
                lower(a.doctor.user.username) like ?2
                or str(a.status) like ?2
            )
            and a.isDeleted = false
            order by a.id desc
            """,
                patientUserId,
                search
        )
                .page(Page.ofSize(SEARCH_LIMIT))
                .list();
    }
}