package com.clinic.exception;

public class ErrorMessage {

    public static class User{
        public static final String NOT_FOUND_USER = "not.found.user";
        public static final String INCORRECT_INFORMATION = "incorrect.login.information";
        public static final String SAVE_INFORMATION = "account.already.exists";
    }

    // Thêm phần cho Appointment
    public static class Appointment {
        public static final String DOCTOR_NOT_FOUND = "doctor.not.found";
        public static final String PATIENT_NOT_FOUND = "patient.not.found";
        public static final String SCHEDULE_CONFLICT = "appointment.schedule.conflict";
        public static final String INVALID_TIME = "appointment.invalid.time";
    }
}
