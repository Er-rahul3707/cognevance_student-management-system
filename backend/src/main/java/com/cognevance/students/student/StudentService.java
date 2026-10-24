package com.cognevance.students.student;

import com.cognevance.students.shared.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;

@Service
public class StudentService {
    private final StudentRepository repository;
    public StudentService(StudentRepository repository) { this.repository = repository; }
    @Transactional(readOnly = true) public List<Student> findAll(String query) {
        return query == null || query.isBlank() ? repository.findAllByOrderByIdDesc() : repository.search(query.trim());
    }
    @Transactional(readOnly = true) public Student findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ApiException.NotFound("Student " + id + " was not found"));
    }
    @Transactional public Student create(StudentRequest request) {
        String email = normalizeEmail(request.email());
        if (repository.existsByEmailIgnoreCase(email)) throw new ApiException.Conflict("A student with this email already exists");
        return repository.saveAndFlush(new Student(clean(request.fullName()), email, clean(request.phone()), clean(request.course()), request.enrollmentDate()));
    }
    @Transactional public Student update(Long id, StudentRequest request) {
        Student student = findById(id);
        String email = normalizeEmail(request.email());
        if (repository.existsByEmailIgnoreCaseAndIdNot(email, id)) throw new ApiException.Conflict("A student with this email already exists");
        student.update(clean(request.fullName()), email, clean(request.phone()), clean(request.course()), request.enrollmentDate());
        return repository.saveAndFlush(student);
    }
    @Transactional public void delete(Long id) { repository.delete(findById(id)); }
    private String clean(String value) { return value == null ? null : value.trim(); }
    private String normalizeEmail(String value) { return value.trim().toLowerCase(Locale.ROOT); }
}
