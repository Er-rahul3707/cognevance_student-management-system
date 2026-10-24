package com.cognevance.students.student;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record StudentRequest(
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Email @Size(max = 180) String email,
        @Pattern(regexp = "^$|^[+()0-9 .-]{7,30}$", message = "must be a valid phone number") String phone,
        @NotBlank @Size(max = 100) String course,
        @NotNull @PastOrPresent LocalDate enrollmentDate) { }
