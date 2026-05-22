package com.cttest.service.impl;

import com.cttest.model.entity.StudentAnswer;
import com.cttest.repository.StudentAnswerRepository;
import com.cttest.service.StudentAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentAnswerServiceImpl implements StudentAnswerService {

    private final StudentAnswerRepository studentAnswerRepository;

    @Override
    @Transactional
    public StudentAnswer save(StudentAnswer studentAnswer) {
        if (studentAnswer.getId() == null) {
            studentAnswer.setAddDate(LocalDateTime.now());
        }
        studentAnswer.setModDate(LocalDateTime.now());
        return studentAnswerRepository.save(studentAnswer);
    }

    @Override
    @Transactional
    public StudentAnswer saveOrUpdate(Integer studentTestId, Integer questionId, Integer answerId) {
        Optional<StudentAnswer> existing = studentAnswerRepository
                .findByStudentTestIdAndQuestionId(studentTestId, questionId);
        if (existing.isPresent()) {
            StudentAnswer sa = existing.get();
            sa.setAnswerId(answerId);
            sa.setModDate(LocalDateTime.now());
            return studentAnswerRepository.save(sa);
        }
        StudentAnswer sa = new StudentAnswer();
        sa.setStudentTestId(studentTestId);
        sa.setQuestionId(questionId);
        sa.setAnswerId(answerId);
        sa.setAddDate(LocalDateTime.now());
        sa.setModDate(LocalDateTime.now());
        return studentAnswerRepository.save(sa);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentAnswer> findByStudentTestId(Integer studentTestId) {
        return studentAnswerRepository.findByStudentTestId(studentTestId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Integer, Integer> findAnswersMapByStudentTestId(Integer studentTestId) {
        return studentAnswerRepository.findByStudentTestId(studentTestId).stream()
                .collect(Collectors.toMap(StudentAnswer::getQuestionId, StudentAnswer::getAnswerId));
    }
}
