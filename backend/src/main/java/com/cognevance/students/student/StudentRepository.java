package com.cognevance.students.student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    @Query("select s from Student s where lower(s.fullName) like lower(concat('%', :q, '%')) or lower(s.email) like lower(concat('%', :q, '%')) or lower(s.course) like lower(concat('%', :q, '%')) or str(s.id) like concat('%', :q, '%') order by s.id desc")
    List<Student> search(@Param("q") String query);
    List<Student> findAllByOrderByIdDesc();
}
