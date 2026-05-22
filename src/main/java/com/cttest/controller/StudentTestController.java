package com.cttest.controller;

import com.cttest.model.entity.StudentTest;
import com.cttest.service.StudentTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/student-tests")
@RequiredArgsConstructor
public class StudentTestController {

    private final StudentTestService studentTestService;

    @GetMapping
    public ResponseEntity<List<StudentTest>> getAll(@RequestParam(required = false) Integer testInstanceId) {
        if (testInstanceId != null) {
            return ResponseEntity.ok(studentTestService.findByTestInstanceId(testInstanceId));
        }
        return ResponseEntity.ok(studentTestService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentTest> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(studentTestService.findById(id));
    }

    @PostMapping
    public ResponseEntity<StudentTest> create(@RequestBody StudentTest studentTest) {
        return ResponseEntity.ok(studentTestService.save(studentTest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentTest> update(@PathVariable Integer id, @RequestBody StudentTest studentTest) {
        studentTest.setId(id);
        return ResponseEntity.ok(studentTestService.update(studentTest));
    }

}
