package com.cttest.service;

import com.cttest.model.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudentService {
    List<Student> findAll();
    Page<Student> findAll(Pageable pageable);
    Page<Student> search(String query, Pageable pageable);
    Student findById(Integer id);
    Student save(Student student);
    Student update(Student student);
    void deleteById(Integer id);
    List<Student> findByStudentGroupId(Integer groupId);
}
