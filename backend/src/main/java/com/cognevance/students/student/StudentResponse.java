package com.cognevance.students.student;

import java.time.Instant;
import java.time.LocalDate;

public record StudentResponse(Long id, String fullName, String email, String phone,
                              String course, LocalDate enrollmentDate, Instant createdAt) {
    public static StudentResponse from(Student student) {
        return new StudentResponse(student.getId(), student.getFullName(), student.getEmail(),
                student.getPhone(), student.getCourse(), student.getEnrollmentDate(), student.getCreatedAt());
    }
}
