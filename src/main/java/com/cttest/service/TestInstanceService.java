package com.cttest.service;

import com.cttest.model.entity.TestInstance;

import java.util.List;
import java.util.Optional;

public interface TestInstanceService extends BaseService<TestInstance, Integer> {
    List<TestInstance> findByTestId(Integer testId);
    Optional<TestInstance> findByPin(Integer pin);
    Optional<Integer> getIdByPin(Integer pin);
    Integer generatePin();
}
