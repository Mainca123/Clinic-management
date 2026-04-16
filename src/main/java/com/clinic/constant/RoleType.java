package com.clinic.constant;

public enum RoleType {
    ADMIN(Constants.ADMIN),
    DOCTOR(Constants.DOCTOR),
    PATIENT(Constants.PATIENT);

    public static class Constants {
        public static final String ADMIN = "admin";
        public static final String DOCTOR = "doctor";
        public static final String PATIENT = "patient";
    }

    private final String label;
    RoleType(String label) { this.label = label; }
}