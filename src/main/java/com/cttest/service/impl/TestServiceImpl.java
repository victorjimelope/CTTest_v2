package com.cttest.service.impl;

import com.cttest.model.entity.Test;
import com.cttest.repository.QuestionRepository;
import com.cttest.repository.TestRepository;
import com.cttest.service.TestService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TestServiceImpl extends BaseServiceImpl<Test, Integer, TestRepository> implements TestService {

    private final QuestionRepository questionRepository;

    public TestServiceImpl(TestRepository repository, QuestionRepository questionRepository) {
        super(repository, "Test");
        this.questionRepository = questionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Test> search(String query, Pageable pageable) {
        return repository.findAll((root, cq, cb) -> {
            Predicate nameLike = cb.like(cb.lower(root.get("name")), "%" + query.toLowerCase() + "%");
            Predicate descLike = cb.like(cb.lower(root.get("description")), "%" + query.toLowerCase() + "%");
            return cb.or(nameLike, descLike);
        }, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Test findWithQuestionsById(Integer id) {
        Test test = findById(id);
        test.setQuestions(questionRepository.findWithAnswersByTestId(id));
        return test;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Test> findByCenterId(Integer centerId) {
        return repository.findByCenterIdOrderByAddDateDesc(centerId);
    }

}
