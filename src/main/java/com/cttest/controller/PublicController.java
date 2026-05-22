package com.cttest.controller;

import com.cttest.model.entity.Question;
import com.cttest.model.entity.StudentAnswer;
import com.cttest.model.entity.StudentTest;
import com.cttest.model.entity.Test;
import com.cttest.model.entity.TestInstance;
import com.cttest.model.dto.AnswerRequest;
import com.cttest.model.dto.PinRequest;
import com.cttest.model.enums.StudentTestStatus;
import com.cttest.service.QuestionService;
import com.cttest.service.StudentAnswerService;
import com.cttest.service.StudentTestService;
import com.cttest.service.TestInstanceService;
import com.cttest.service.TestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final TestInstanceService testInstanceService;
    private final StudentTestService studentTestService;
    private final TestService testService;
    private final QuestionService questionService;
    private final StudentAnswerService studentAnswerService;

    @PostMapping("/validate-pin")
    public ResponseEntity<Map<String, Object>> validatePin(@Valid @RequestBody PinRequest request) {
        Optional<Integer> testInstanceId = testInstanceService.getIdByPin(request.pin());
        if (testInstanceId.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid PIN"));
        }

        Optional<StudentTest> studentTest = studentTestService.findByTestInstanceAndStudent(
                testInstanceId.get(), request.studentId());

        if (studentTest.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Student not assigned to this test"));
        }

        Integer studentTestId = studentTest.get().getId();
        return ResponseEntity.ok(Map.of("studentTestId", studentTestId));
    }

    @GetMapping("/student-tests/{id}/test-data")
    public ResponseEntity<Map<String, Object>> getTestData(@PathVariable Integer id) {
        StudentTest studentTest = studentTestService.findById(id);

        TestInstance instance = testInstanceService.findById(studentTest.getTestInstanceId());
        Test test = testService.findWithQuestionsById(instance.getTestId());
        Map<Integer, Integer> answers = studentAnswerService.findAnswersMapByStudentTestId(id);

        Map<String, Object> data = new HashMap<>();
        data.put("studentTest", studentTest);
        data.put("testInstance", instance);
        data.put("test", test);
        data.put("answers", answers);

        if (studentTest.isClosed()) {
            data.put("readonly", true);
        }

        return ResponseEntity.ok(data);
    }

    @GetMapping("/student-tests/{id}/result")
    public ResponseEntity<Map<String, Object>> getResult(@PathVariable Integer id) {
        StudentTest studentTest = studentTestService.findById(id);
        Test test = testService.findWithQuestionsById(
                testInstanceService.findById(studentTest.getTestInstanceId()).getTestId());
        Map<Integer, Integer> answers = studentAnswerService.findAnswersMapByStudentTestId(id);

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

        Map<String, Object> result = new HashMap<>();
        result.put("score", studentTest.getScore());
        result.put("correctCount", correctCount);
        result.put("totalQuestions", totalQuestions);
        result.put("studentTest", studentTest);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/student-tests/{id}/answer")
    public ResponseEntity<Void> saveAnswer(@PathVariable Integer id, @Valid @RequestBody AnswerRequest request) {
        StudentTest studentTest = studentTestService.findById(id);
        if (studentTest.isClosed()) {
            return ResponseEntity.badRequest().build();
        }

        if (studentTest.getStatus() == StudentTestStatus.ASSIGNED) {
            studentTest.setStatus(StudentTestStatus.IN_PROGRESS);
            studentTest.setStartDate(LocalDateTime.now());
            studentTestService.update(studentTest);
        }

        StudentAnswer answer = new StudentAnswer();
        answer.setStudentTestId(id);
        answer.setQuestionId(request.questionId());
        answer.setAnswerId(request.answerId());
        studentAnswerService.save(answer);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/student-tests/{id}/finish")
    public ResponseEntity<Map<String, Object>> finishTest(@PathVariable Integer id) {
        StudentTest studentTest = studentTestService.findById(id);
        if (studentTest.isClosed()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Test already finished"));
        }

        TestInstance instance = testInstanceService.findById(studentTest.getTestInstanceId());
        Test test = testService.findWithQuestionsById(instance.getTestId());
        Map<Integer, Integer> answers = studentAnswerService.findAnswersMapByStudentTestId(id);

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

        studentTest.setStatus(StudentTestStatus.FINISHED);
        studentTest.setFinishDate(LocalDateTime.now());
        studentTest.setScore(score);
        studentTestService.update(studentTest);

        Map<String, Object> result = new HashMap<>();
        result.put("score", score);
        result.put("correctCount", correctCount);
        result.put("totalQuestions", totalQuestions);
        result.put("studentTest", studentTest);
        return ResponseEntity.ok(result);
    }
}
