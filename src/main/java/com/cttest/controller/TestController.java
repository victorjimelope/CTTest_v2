package com.cttest.controller;

import com.cttest.model.entity.Test;
import com.cttest.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import java.net.URI;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) Integer centerId,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        if (centerId != null) {
            return ResponseEntity.ok(testService.findByCenterId(centerId));
        }
        if (search != null) {
            return ResponseEntity.ok(testService.search(search, pageable));
        }
        return ResponseEntity.ok(testService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Test> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(testService.findById(id));
    }

    @GetMapping("/{id}/with-questions")
    public ResponseEntity<Test> getWithQuestions(@PathVariable Integer id) {
        return ResponseEntity.ok(testService.findWithQuestionsById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Test> create(@Valid @RequestBody Test test) {
        Test saved = testService.save(test);
        return ResponseEntity.created(URI.create("/api/tests/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Test> update(@PathVariable Integer id, @Valid @RequestBody Test test) {
        test.setId(id);
        return ResponseEntity.ok(testService.update(test));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        testService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
