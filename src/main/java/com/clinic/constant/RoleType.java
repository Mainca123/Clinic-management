package com.clinic.constant;

public enum RoleType {
    ADMIN(Constants.ADMIN),
    DOCTOR(Constants.DOCTOR),
    PATIENT(Constants.PATIENT);

    public static class Constants {
        public static final String ADMIN = "ADMIN";
        public static final String DOCTOR = "DOCTOR";
        public static final String PATIENT = "PATIENT";
    }

    private final String label;
    RoleType(String label) { this.label = label; }
}