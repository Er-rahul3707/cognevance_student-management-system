package com.cognevance.students.shared;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.LinkedHashMap;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.NotFound.class) ResponseEntity<ApiError> notFound(ApiException ex, HttpServletRequest req) { return response(HttpStatus.NOT_FOUND, ex.getMessage()); }
    @ExceptionHandler(ApiException.Conflict.class) ResponseEntity<ApiError> conflict(ApiException ex) { return response(HttpStatus.CONFLICT, ex.getMessage()); }
    @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ApiError> invalid(MethodArgumentNotValidException ex) {
        var errors = new LinkedHashMap<String, String>(); ex.getBindingResult().getFieldErrors().forEach(e -> errors.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ApiError(Instant.now(), 400, "Bad Request", "Please correct the highlighted fields", errors));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class) ResponseEntity<ApiError> malformed() { return response(HttpStatus.BAD_REQUEST, "Request body is missing or malformed"); }
    @ExceptionHandler(DataAccessException.class) ResponseEntity<ApiError> database() { return response(HttpStatus.SERVICE_UNAVAILABLE, "The database is temporarily unavailable"); }
    @ExceptionHandler(Exception.class) ResponseEntity<ApiError> unexpected() { return response(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"); }
    private ResponseEntity<ApiError> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, java.util.Map.of()));
    }
}
