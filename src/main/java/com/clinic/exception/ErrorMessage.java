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

    public static class Medicine {
        public static final String NOT_FOUND = "medicine.not.found";
        public static final String ALREADY_EXISTS = "medicine.already.exists";
        public static final String IN_USE = "medicine.is.currently.in.use"; // Khi thuốc đã có trong đơn thuốc thì không được xóa
    }

    public static class MedicalRecord {
        public static final String NOT_FOUND = "medical.record.not.found";
        public static final String INVALID_DATA = "medical.record.invalid.data";
    }
}
