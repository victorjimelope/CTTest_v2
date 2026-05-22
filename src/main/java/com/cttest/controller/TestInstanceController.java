package com.cttest.controller;

import com.cttest.model.entity.TestInstance;
import com.cttest.service.TestInstanceService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test-instances")
public class TestInstanceController extends BaseController<TestInstance, TestInstanceService> {

    public TestInstanceController(TestInstanceService service) {
        super(service);
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) Integer testId,
            Pageable pageable) {
        if (testId != null) {
            return ResponseEntity.ok(service.findByTestId(testId));
        }
        return listAll(null, pageable);
    }

    @Override
    protected String getPathSegment() {
        return "test-instances";
    }
}
