package com.cttest.service;

import com.cttest.model.entity.TestInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TestInstanceService {
    List<TestInstance> findAll();
    Page<TestInstance> findAll(Pageable pageable);
    TestInstance findById(Integer id);
    TestInstance save(TestInstance testInstance);
    TestInstance update(TestInstance testInstance);
    void deleteById(Integer id);
    List<TestInstance> findByTestId(Integer testId);
    Optional<TestInstance> findByPin(Integer pin);
    Optional<Integer> getIdByPin(Integer pin);
    Integer generatePin();
}
