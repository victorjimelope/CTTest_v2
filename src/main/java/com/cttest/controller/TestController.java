package com.cttest.controller;

import com.cttest.model.entity.Test;
import com.cttest.service.TestService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tests")
public class TestController extends BaseController<Test, TestService> {

    public TestController(TestService service) {
        super(service);
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) Integer centerId,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        if (centerId != null) {
            return ResponseEntity.ok(service.findByCenterId(centerId));
        }
        return listAll(search, pageable);
    }

    @GetMapping("/{id}/with-questions")
    public ResponseEntity<Test> getWithQuestions(@PathVariable Integer id) {
        return ResponseEntity.ok(service.findWithQuestionsById(id));
    }

    @Override
    protected String getPathSegment() {
        return "tests";
    }
}
