package com.cttest.controller;

import com.cttest.model.entity.Test;
import com.cttest.model.dto.EntityDTOMapper;
import com.cttest.model.dto.TestDTO;
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
public class TestController extends BaseController<Test, TestDTO, TestService> {

    public TestController(TestService service) {
        super(service);
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) Integer centerId,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        if (centerId != null) {
            return ResponseEntity.ok(service.findByCenterId(centerId).stream().map(this::toDto).toList());
        }
        return listAll(search, pageable);
    }

    @GetMapping("/{id}/with-questions")
    public ResponseEntity<TestDTO> getWithQuestions(@PathVariable Integer id) {
        return ResponseEntity.ok(toDto(service.findWithQuestionsById(id)));
    }

    @Override
    protected TestDTO toDto(Test entity) {
        return EntityDTOMapper.toDTO(entity);
    }

    @Override
    protected String getPathSegment() {
        return "tests";
    }
}
