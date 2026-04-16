package com.clinic.base;

import com.fasterxml.jackson.annotation.JsonInclude;

public record RestData<T>(
        String status,
        String message,
        @JsonInclude(JsonInclude.Include.NON_NULL) T data
) {

    public static <T> RestData<T> success(T data) {
        return new RestData<>("SUCCESS", "Operation successful", data);
    }

    public static <T> RestData<T> success(String message, T data) {
        return new RestData<>("SUCCESS", message, data);
    }

    public static <T> RestData<T> success(String message) {
        return new RestData<>("SUCCESS", message, null);
    }

    public static <T> RestData<T> error(String message) {
        return new RestData<>("ERROR", message, null);
    }
}