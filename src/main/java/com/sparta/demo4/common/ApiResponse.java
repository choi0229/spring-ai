package com.sparta.demo4.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

public record ApiResponse<T>(boolean result, T data, Error error) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, null, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(success(data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(String code, String message) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, Error.of(code, message)));
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(String code, String message, List<String> details) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, Error.of(code, message, details)));
    }

    public static <T> ResponseEntity<ApiResponse<T>> serverError(String code, String message) {
        return ResponseEntity.internalServerError().body(new ApiResponse<>(false, null, Error.of(code, message)));
    }

    public record Error(String code, String message, List<String> details) {
        public static Error of(String code, String message) {
            return new Error(code, message, Collections.emptyList());
        }

        public static Error of(String code, String message, List<String> details) {
            return new Error(code, message, details == null ? Collections.emptyList() : List.copyOf(details));
        }
    }
}
