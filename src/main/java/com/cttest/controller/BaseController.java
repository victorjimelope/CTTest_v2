package com.cttest.controller;

import com.cttest.model.entity.BaseEntity;
import com.cttest.service.BaseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

public abstract class BaseController<T extends BaseEntity, S extends BaseService<T, Integer>> {

    protected final S service;

    public BaseController(S service) {
        this.service = service;
    }

    protected ResponseEntity<?> listAll(String search, Pageable pageable) {
        if (search != null) {
            return ResponseEntity.ok(service.search(search, pageable));
        }
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<T> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<T> create(@Valid @RequestBody T entity) {
        T saved = service.save(entity);
        return ResponseEntity.created(
                java.net.URI.create("/api/" + getPathSegment() + "/" + saved.getId())
        ).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<T> update(@PathVariable Integer id, @Valid @RequestBody T entity) {
        entity.setId(id);
        return ResponseEntity.ok(service.update(entity));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    protected abstract String getPathSegment();
}
