package com.cttest.service.impl;

import com.cttest.model.entity.StudentGroup;
import com.cttest.repository.StudentGroupRepository;
import com.cttest.service.StudentGroupService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentGroupServiceImpl extends BaseServiceImpl<StudentGroup, Integer, StudentGroupRepository> implements StudentGroupService {

    public StudentGroupServiceImpl(StudentGroupRepository repository) {
        super(repository, "StudentGroup");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentGroup> search(String query, Pageable pageable) {
        return repository.findAll((root, cq, cb) -> {
            Predicate nameLike = cb.like(cb.lower(root.get("name")), "%" + query.toLowerCase() + "%");
            return cb.or(nameLike);
        }, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentGroup> findByCenterId(Integer centerId) {
        return repository.findByCenterIdOrderByAddDateDesc(centerId);
    }

}
