package com.cttest.controller;

import com.cttest.model.entity.Question;
import com.cttest.model.entity.Test;
import com.cttest.service.QuestionService;
import com.cttest.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final TestService testService;

    @GetMapping("/questions/{id}")
    public ResponseEntity<Question> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(questionService.findById(id));
    }

    @PostMapping("/tests/{testId}/questions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Question> create(@PathVariable Integer testId, @Valid @RequestBody Question question) {
        Test test = testService.findById(testId);
        question.setTest(test);
        question.getAnswers().forEach(a -> a.setQuestion(question));
        Question saved = questionService.save(question);
        return ResponseEntity.created(URI.create("/api/questions/" + saved.getId())).body(saved);
    }

    @PutMapping("/questions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Question> update(@PathVariable Integer id, @Valid @RequestBody Question question) {
        question.setId(id);
        question.getAnswers().forEach(a -> a.setQuestion(question));
        return ResponseEntity.ok(questionService.update(question));
    }

    @DeleteMapping("/questions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        questionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/questions/{id}/move")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> move(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        questionService.move(id, body.getOrDefault("direction", "up"));
        return ResponseEntity.noContent().build();
    }
}
