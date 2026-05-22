package com.cttest.service;

import com.cttest.model.entity.Center;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CenterService {
    List<Center> findAll();
    Page<Center> findAll(Pageable pageable);
    Page<Center> search(String query, Pageable pageable);
    Center findById(Integer id);
    Center save(Center center);
    Center update(Center center);
    void deleteById(Integer id);
}
