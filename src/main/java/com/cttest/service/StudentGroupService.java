package com.cttest.service;

import com.cttest.model.entity.StudentGroup;

import java.util.List;

public interface StudentGroupService extends BaseService<StudentGroup, Integer> {
    List<StudentGroup> findByCenterId(Integer centerId);
}
