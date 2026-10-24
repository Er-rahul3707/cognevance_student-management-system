package com.cognevance.students.shared;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.util.LinkedHashMap;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(ApiException.NotFound.class) ResponseEntity<ApiError> notFound(ApiException ex) { return response(HttpStatus.NOT_FOUND, ex.getMessage()); }
    @ExceptionHandler(ApiException.Conflict.class) ResponseEntity<ApiError> conflict(ApiException ex) { return response(HttpStatus.CONFLICT, ex.getMessage()); }
    @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ApiError> invalid(MethodArgumentNotValidException ex) {
        var errors = new LinkedHashMap<String, String>(); ex.getBindingResult().getFieldErrors().forEach(e -> errors.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ApiError(Instant.now(), 400, "Bad Request", "Please correct the highlighted fields", errors));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class) ResponseEntity<ApiError> malformed() { return response(HttpStatus.BAD_REQUEST, "Request body is missing or malformed"); }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class) ResponseEntity<ApiError> invalidParameter() { return response(HttpStatus.BAD_REQUEST, "A path or query parameter has an invalid format"); }
    @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<ApiError> constraintConflict() { return response(HttpStatus.CONFLICT, "The request conflicts with an existing student record"); }
    @ExceptionHandler(DataAccessException.class) ResponseEntity<ApiError> database() { return response(HttpStatus.SERVICE_UNAVAILABLE, "The database is temporarily unavailable"); }
    @ExceptionHandler(Exception.class) ResponseEntity<ApiError> unexpected(Exception ex) {
        log.error("Unexpected API error", ex);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }
    private ResponseEntity<ApiError> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, java.util.Map.of()));
    }
}
