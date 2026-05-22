package com.cttest.service.impl;

import com.cttest.model.entity.Center;
import com.cttest.repository.CenterRepository;
import com.cttest.service.CenterService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CenterServiceImpl extends BaseServiceImpl<Center, Integer, CenterRepository> implements CenterService {

    public CenterServiceImpl(CenterRepository repository) {
        super(repository, "Center");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Center> search(String query, Pageable pageable) {
        return repository.findAll((root, cq, cb) -> {
            Predicate nameLike = cb.like(cb.lower(root.get("name")), "%" + query.toLowerCase() + "%");
            return cb.or(nameLike);
        }, pageable);
    }
}
