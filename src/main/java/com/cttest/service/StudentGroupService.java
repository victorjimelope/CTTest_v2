package com.cttest.service;

import com.cttest.model.entity.StudentGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudentGroupService {
    List<StudentGroup> findAll();
    Page<StudentGroup> findAll(Pageable pageable);
    Page<StudentGroup> search(String query, Pageable pageable);
    StudentGroup findById(Integer id);
    StudentGroup save(StudentGroup studentGroup);
    StudentGroup update(StudentGroup studentGroup);
    void deleteById(Integer id);
    List<StudentGroup> findByCenterId(Integer centerId);
}
