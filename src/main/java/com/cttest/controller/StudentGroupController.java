package com.cttest.controller;

import com.cttest.model.entity.StudentGroup;
import com.cttest.model.dto.EntityDTOMapper;
import com.cttest.model.dto.StudentGroupDTO;
import com.cttest.service.StudentGroupService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student-groups")
public class StudentGroupController extends BaseController<StudentGroup, StudentGroupDTO, StudentGroupService> {

    public StudentGroupController(StudentGroupService service) {
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

    @Override
    protected StudentGroupDTO toDto(StudentGroup entity) {
        return EntityDTOMapper.toDTO(entity);
    }

    @Override
    protected String getPathSegment() {
        return "student-groups";
    }
}
