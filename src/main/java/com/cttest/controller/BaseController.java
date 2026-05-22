package com.cttest.controller;

import com.cttest.model.entity.BaseEntity;
import com.cttest.service.BaseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

public abstract class BaseController<T extends BaseEntity, D, S extends BaseService<T, Integer>> {

    protected final S service;

    public BaseController(S service) {
        this.service = service;
    }

    protected abstract D toDto(T entity);

    protected ResponseEntity<?> listAll(String search, Pageable pageable) {
        if (search != null) {
            return ResponseEntity.ok(service.search(search, pageable).map(this::toDto));
        }
        return ResponseEntity.ok(service.findAll(pageable).map(this::toDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<D> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(toDto(service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<D> create(@Valid @RequestBody T entity) {
        T saved = service.save(entity);
        return ResponseEntity.created(
                java.net.URI.create("/api/" + getPathSegment() + "/" + saved.getId())
        ).body(toDto(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<D> update(@PathVariable Integer id, @Valid @RequestBody T entity) {
        entity.setId(id);
        return ResponseEntity.ok(toDto(service.update(entity)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    protected abstract String getPathSegment();
}
