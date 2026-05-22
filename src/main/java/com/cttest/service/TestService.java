package com.cttest.service;

import com.cttest.model.entity.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TestService {
    List<Test> findAll();
    Page<Test> findAll(Pageable pageable);
    Page<Test> search(String query, Pageable pageable);
    Test findById(Integer id);
    Test findWithQuestionsById(Integer id);
    Test save(Test test);
    Test update(Test test);
    void deleteById(Integer id);
    List<Test> findByCenterId(Integer centerId);
}
