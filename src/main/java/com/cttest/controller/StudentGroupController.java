package com.cttest.controller;

import com.cttest.model.entity.StudentGroup;
import com.cttest.service.StudentGroupService;
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

@RestController
@RequestMapping("/api/student-groups")
@RequiredArgsConstructor
public class StudentGroupController {

    private final StudentGroupService studentGroupService;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) Integer centerId,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        if (centerId != null) {
            return ResponseEntity.ok(studentGroupService.findByCenterId(centerId));
        }
        if (search != null) {
            return ResponseEntity.ok(studentGroupService.search(search, pageable));
        }
        return ResponseEntity.ok(studentGroupService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentGroup> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(studentGroupService.findById(id));
    }

    @PostMapping
    public ResponseEntity<StudentGroup> create(@RequestBody StudentGroup studentGroup) {
        StudentGroup saved = studentGroupService.save(studentGroup);
        return ResponseEntity.created(URI.create("/api/student-groups/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentGroup> update(@PathVariable Integer id, @RequestBody StudentGroup studentGroup) {
        studentGroup.setId(id);
        return ResponseEntity.ok(studentGroupService.update(studentGroup));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        studentGroupService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
