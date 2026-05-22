package com.cttest.controller;

import com.cttest.model.entity.Center;
import com.cttest.service.CenterService;
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
@RequestMapping("/api/centers")
@RequiredArgsConstructor
public class CenterController {

    private final CenterService centerService;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        if (search != null) {
            return ResponseEntity.ok(centerService.search(search, pageable));
        }
        return ResponseEntity.ok(centerService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Center> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(centerService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Center> create(@Valid @RequestBody Center center) {
        Center saved = centerService.save(center);
        return ResponseEntity.created(URI.create("/api/centers/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Center> update(@PathVariable Integer id, @Valid @RequestBody Center center) {
        center.setId(id);
        return ResponseEntity.ok(centerService.update(center));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        centerService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
