package com.cognevance.students.student;

import com.cognevance.students.shared.ApiException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

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
        if (repository.existsByEmailIgnoreCase(request.email().trim())) throw new ApiException.Conflict("A student with this email already exists");
        return persist(new Student(clean(request.fullName()), request.email().trim(), clean(request.phone()), clean(request.course()), request.enrollmentDate()));
    }
    @Transactional public Student update(Long id, StudentRequest request) {
        Student student = findById(id);
        if (repository.existsByEmailIgnoreCaseAndIdNot(request.email().trim(), id)) throw new ApiException.Conflict("A student with this email already exists");
        student.update(clean(request.fullName()), request.email().trim(), clean(request.phone()), clean(request.course()), request.enrollmentDate());
        return persist(student);
    }
    @Transactional public void delete(Long id) { repository.delete(findById(id)); }
    private Student persist(Student student) {
        try { return repository.saveAndFlush(student); }
        catch (DataIntegrityViolationException ex) { throw new ApiException.Conflict("A student with this email already exists"); }
    }
    private String clean(String value) { return value == null ? null : value.trim(); }
}
