package com.cttest.service;

import com.cttest.model.entity.StudentTest;

import java.util.List;
import java.util.Optional;

public interface StudentTestService {
    List<StudentTest> findAll();
    StudentTest findById(Integer id);
    StudentTest save(StudentTest studentTest);
    StudentTest update(StudentTest studentTest);
    void deleteById(Integer id);
    List<StudentTest> findByTestInstanceId(Integer testInstanceId);
    Optional<StudentTest> findByTestInstanceAndStudent(Integer testInstanceId, Integer studentId);
    List<StudentTest> findByStudentId(Integer studentId);
}
