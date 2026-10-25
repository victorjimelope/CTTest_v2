package com.cttest.service.impl;

import com.cttest.model.dto.ScoreResult;
import com.cttest.model.entity.Question;
import com.cttest.model.entity.StudentTest;
import com.cttest.exception.ResourceNotFoundException;
import com.cttest.repository.StudentTestRepository;
import com.cttest.service.StudentTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentTestServiceImpl implements StudentTestService {

    private final StudentTestRepository studentTestRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StudentTest> findAll() {
        return studentTestRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentTest findById(Integer id) {
        return studentTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StudentTest not found with id: " + id));
    }

    @Override
    @Transactional
    public StudentTest save(StudentTest studentTest) {
        return studentTestRepository.save(studentTest);
    }

    @Override
    @Transactional
    public StudentTest update(StudentTest studentTest) {
        StudentTest existing = findById(studentTest.getId());
        existing.setComment(studentTest.getComment());
        existing.setModDate(LocalDateTime.now());
        return studentTestRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentTest> findByTestInstanceId(Integer testInstanceId) {
        return studentTestRepository.findByTestInstanceId(testInstanceId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StudentTest> findByTestInstanceAndStudent(Integer testInstanceId, Integer studentId) {
        return studentTestRepository.findByTestInstanceIdAndStudentId(testInstanceId, studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentTest> findByStudentId(Integer studentId) {
        return studentTestRepository.findByStudentId(studentId);
    }

    @Override
    public ScoreResult calculateScore(com.cttest.model.entity.Test test, Map<Integer, Integer> answers) {
        int totalQuestions = test.getQuestions().size();
        int correctCount = 0;
        for (Question q : test.getQuestions()) {
            Integer selectedAnswerId = answers.get(q.getId());
            if (selectedAnswerId != null) {
                boolean isCorrect = q.getAnswers().stream()
                        .anyMatch(a -> a.getId().equals(selectedAnswerId) && a.isCorrect());
                if (isCorrect) correctCount++;
            }
        }
        BigDecimal score = totalQuestions > 0
                ? BigDecimal.valueOf(correctCount * 100L / totalQuestions)
                : BigDecimal.ZERO;
        return new ScoreResult(totalQuestions, correctCount, score, answers);
    }

}
