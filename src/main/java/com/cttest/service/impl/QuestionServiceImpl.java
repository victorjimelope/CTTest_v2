package com.cttest.service.impl;

import com.cttest.model.entity.Question;
import com.cttest.exception.ResourceNotFoundException;
import com.cttest.repository.QuestionRepository;
import com.cttest.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;

    @Override
    @Transactional(readOnly = true)
    public Question findById(Integer id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));
    }

    @Override
    @Transactional
    public Question save(Question question) {
        return questionRepository.save(question);
    }

    @Override
    @Transactional
    public Question update(Question question) {
        return questionRepository.save(question);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        questionRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void move(Integer id, String direction) {
        Question current = findById(id);
        List<Question> siblings = questionRepository.findByTestIdOrderByOrderAsc(current.getTest().getId());
        int idx = siblings.indexOf(current);
        if ("up".equals(direction) && idx > 0) {
            Collections.swap(siblings, idx, idx - 1);
        } else if ("down".equals(direction) && idx < siblings.size() - 1) {
            Collections.swap(siblings, idx, idx + 1);
        }
        for (int i = 0; i < siblings.size(); i++) {
            siblings.get(i).setOrder(i + 1);
        }
        questionRepository.saveAll(siblings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Question> findByTestId(Integer testId) {
        return questionRepository.findByTestIdOrderByOrderAsc(testId);
    }
}
