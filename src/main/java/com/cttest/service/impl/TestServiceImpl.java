package com.cttest.service.impl;

import com.cttest.model.entity.Test;
import com.cttest.repository.QuestionRepository;
import com.cttest.repository.TestRepository;
import com.cttest.service.TestService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final TestRepository testRepository;
    private final QuestionRepository questionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Test> findAll() {
        return testRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Test> findAll(Pageable pageable) {
        return testRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Test> search(String query, Pageable pageable) {
        return testRepository.findAll((root, cq, cb) -> {
            Predicate nameLike = cb.like(cb.lower(root.get("name")), "%" + query.toLowerCase() + "%");
            Predicate descLike = cb.like(cb.lower(root.get("description")), "%" + query.toLowerCase() + "%");
            return cb.or(nameLike, descLike);
        }, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Test findById(Integer id) {
        return testRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Test findWithQuestionsById(Integer id) {
        Test test = findById(id);
        test.setQuestions(questionRepository.findWithAnswersByTestId(id));
        return test;
    }

    @Override
    @Transactional
    public Test save(Test test) {
        return testRepository.save(test);
    }

    @Override
    @Transactional
    public Test update(Test test) {
        return testRepository.save(test);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        testRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Test> findByCenterId(Integer centerId) {
        return testRepository.findByCenterIdOrderByAddDateDesc(centerId);
    }

}
