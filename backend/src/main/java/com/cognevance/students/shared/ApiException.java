package com.cognevance.students.shared;

public class ApiException extends RuntimeException {
    private ApiException(String message) { super(message); }
    public static final class NotFound extends ApiException { public NotFound(String message) { super(message); } }
    public static final class Conflict extends ApiException { public Conflict(String message) { super(message); } }
}
