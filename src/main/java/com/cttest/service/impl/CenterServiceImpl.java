package com.cttest.service.impl;

import com.cttest.model.entity.Center;
import com.cttest.exception.ResourceNotFoundException;
import com.cttest.repository.CenterRepository;
import com.cttest.service.CenterService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CenterServiceImpl implements CenterService {

    private final CenterRepository centerRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Center> findAll() {
        return centerRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Center> findAll(Pageable pageable) {
        return centerRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Center> search(String query, Pageable pageable) {
        return centerRepository.findAll((root, cq, cb) -> {
            Predicate nameLike = cb.like(cb.lower(root.get("name")), "%" + query.toLowerCase() + "%");
            return cb.or(nameLike);
        }, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Center findById(Integer id) {
        return centerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Center not found with id: " + id));
    }

    @Override
    @Transactional
    public Center save(Center center) {
        return centerRepository.save(center);
    }

    @Override
    @Transactional
    public Center update(Center center) {
        return centerRepository.save(center);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        centerRepository.deleteById(id);
    }

}
