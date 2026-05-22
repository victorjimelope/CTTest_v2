package com.cttest.service.impl;

import com.cttest.model.entity.StudentGroup;
import com.cttest.repository.StudentGroupRepository;
import com.cttest.service.StudentGroupService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentGroupServiceImpl implements StudentGroupService {

    private final StudentGroupRepository studentGroupRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StudentGroup> findAll() {
        return studentGroupRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentGroup> findAll(Pageable pageable) {
        return studentGroupRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentGroup> search(String query, Pageable pageable) {
        return studentGroupRepository.findAll((root, cq, cb) -> {
            Predicate nameLike = cb.like(cb.lower(root.get("name")), "%" + query.toLowerCase() + "%");
            return cb.or(nameLike);
        }, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentGroup findById(Integer id) {
        return studentGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("StudentGroup not found with id: " + id));
    }

    @Override
    @Transactional
    public StudentGroup save(StudentGroup studentGroup) {
        return studentGroupRepository.save(studentGroup);
    }

    @Override
    @Transactional
    public StudentGroup update(StudentGroup studentGroup) {
        return studentGroupRepository.save(studentGroup);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        studentGroupRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentGroup> findByCenterId(Integer centerId) {
        return studentGroupRepository.findByCenterIdOrderByAddDateDesc(centerId);
    }

}
