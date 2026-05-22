package com.cttest.controller;

import com.cttest.model.entity.TestInstance;
import com.cttest.service.TestInstanceService;
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
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/test-instances")
@RequiredArgsConstructor
public class TestInstanceController {

    private final TestInstanceService testInstanceService;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) Integer testId,
            Pageable pageable) {
        if (testId != null) {
            return ResponseEntity.ok(testInstanceService.findByTestId(testId));
        }
        return ResponseEntity.ok(testInstanceService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestInstance> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(testInstanceService.findById(id));
    }

    @PostMapping
    public ResponseEntity<TestInstance> create(@Valid @RequestBody TestInstance testInstance) {
        if (testInstance.getPin() == null) {
            testInstance.setPin(testInstanceService.generatePin());
        }
        TestInstance saved = testInstanceService.save(testInstance);
        return ResponseEntity.created(URI.create("/api/test-instances/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TestInstance> update(@PathVariable Integer id, @Valid @RequestBody TestInstance testInstance) {
        testInstance.setId(id);
        return ResponseEntity.ok(testInstanceService.update(testInstance));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        testInstanceService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
