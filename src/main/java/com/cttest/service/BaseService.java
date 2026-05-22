package com.cttest.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BaseService<T, ID> {

    List<T> findAll();

    Page<T> findAll(Pageable pageable);

    default Page<T> search(String query, Pageable pageable) {
        return findAll(pageable);
    }

    T findById(ID id);

    T save(T entity);

    T update(T entity);

    void deleteById(ID id);
}
