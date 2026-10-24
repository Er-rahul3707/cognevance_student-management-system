package com.cognevance.students.student;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "students", uniqueConstraints = @UniqueConstraint(name = "uk_students_email", columnNames = "email"))
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 120) private String fullName;
    @Column(nullable = false, length = 180) private String email;
    @Column(length = 30) private String phone;
    @Column(nullable = false, length = 100) private String course;
    @Column(nullable = false) private LocalDate enrollmentDate;
    @Column(nullable = false, updatable = false) private Instant createdAt;

    protected Student() {}
    public Student(String fullName, String email, String phone, String course, LocalDate enrollmentDate) {
        this.fullName = fullName; this.email = email; this.phone = phone; this.course = course;
        this.enrollmentDate = enrollmentDate;
    }
    @PrePersist void onCreate() { if (createdAt == null) createdAt = Instant.now(); }
    public Long getId() { return id; } public String getFullName() { return fullName; }
    public String getEmail() { return email; } public String getPhone() { return phone; }
    public String getCourse() { return course; } public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public Instant getCreatedAt() { return createdAt; }
    public void update(String fullName, String email, String phone, String course, LocalDate enrollmentDate) {
        this.fullName = fullName; this.email = email; this.phone = phone; this.course = course; this.enrollmentDate = enrollmentDate;
    }
}
