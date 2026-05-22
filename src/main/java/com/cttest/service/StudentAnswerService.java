package com.cttest.service;

import com.cttest.model.entity.StudentAnswer;

import java.util.List;
import java.util.Map;

public interface StudentAnswerService {
    StudentAnswer save(StudentAnswer studentAnswer);
    StudentAnswer saveOrUpdate(Integer studentTestId, Integer questionId, Integer answerId);
    List<StudentAnswer> findByStudentTestId(Integer studentTestId);
    Map<Integer, Integer> findAnswersMapByStudentTestId(Integer studentTestId);
}
