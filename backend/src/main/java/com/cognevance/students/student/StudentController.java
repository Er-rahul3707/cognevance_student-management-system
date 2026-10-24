package com.cognevance.students.student;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {
    private final StudentService service;
    public StudentController(StudentService service) { this.service = service; }
    @GetMapping public List<StudentResponse> list(@RequestParam(required = false) String q) { return service.findAll(q).stream().map(StudentResponse::from).toList(); }
    @GetMapping("/{id}") public StudentResponse get(@PathVariable Long id) { return StudentResponse.from(service.findById(id)); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public StudentResponse create(@Valid @RequestBody StudentRequest request) { return StudentResponse.from(service.create(request)); }
    @PutMapping("/{id}") public StudentResponse update(@PathVariable Long id, @Valid @RequestBody StudentRequest request) { return StudentResponse.from(service.update(id, request)); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { service.delete(id); }
}
