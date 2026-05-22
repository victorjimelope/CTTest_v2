package com.cttest.service;

import com.cttest.model.entity.Question;

import java.util.List;

public interface QuestionService {
    Question findById(Integer id);
    Question save(Question question);
    Question update(Question question);
    void deleteById(Integer id);
    void move(Integer id, String direction);
    List<Question> findByTestId(Integer testId);
}
