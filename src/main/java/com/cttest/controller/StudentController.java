package com.cttest.controller;

import com.cttest.model.entity.Student;
import com.cttest.model.dto.EntityDTOMapper;
import com.cttest.model.dto.StudentDTO;
import com.cttest.service.StudentService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students")
public class StudentController extends BaseController<Student, StudentDTO, StudentService> {

    public StudentController(StudentService service) {
        super(service);
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) Integer groupId,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        if (groupId != null) {
            return ResponseEntity.ok(service.findByStudentGroupId(groupId).stream().map(this::toDto).toList());
        }
        return listAll(search, pageable);
    }

    @Override
    protected StudentDTO toDto(Student entity) {
        return EntityDTOMapper.toDTO(entity);
    }

    @Override
    protected String getPathSegment() {
        return "students";
    }
}
