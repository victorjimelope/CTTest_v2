package com.cttest.service;

import com.cttest.model.entity.Student;

import java.util.List;

public interface StudentService extends BaseService<Student, Integer> {
    List<Student> findByStudentGroupId(Integer groupId);
}
