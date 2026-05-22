package com.cttest.controller;

import com.cttest.model.entity.TestInstance;
import com.cttest.model.dto.EntityDTOMapper;
import com.cttest.model.dto.TestInstanceDTO;
import com.cttest.service.TestInstanceService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test-instances")
public class TestInstanceController extends BaseController<TestInstance, TestInstanceDTO, TestInstanceService> {

    public TestInstanceController(TestInstanceService service) {
        super(service);
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) Integer testId,
            Pageable pageable) {
        if (testId != null) {
            return ResponseEntity.ok(service.findByTestId(testId).stream().map(this::toDto).toList());
        }
        return listAll(null, pageable);
    }

    @Override
    protected TestInstanceDTO toDto(TestInstance entity) {
        return EntityDTOMapper.toDTO(entity);
    }

    @Override
    protected String getPathSegment() {
        return "test-instances";
    }
}
