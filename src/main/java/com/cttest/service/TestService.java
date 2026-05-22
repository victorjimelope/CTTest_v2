package com.cttest.service;

import com.cttest.model.entity.Test;

import java.util.List;

public interface TestService extends BaseService<Test, Integer> {
    Test findWithQuestionsById(Integer id);
    List<Test> findByCenterId(Integer centerId);
}
