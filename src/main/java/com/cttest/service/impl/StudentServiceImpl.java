package com.cttest.service.impl;

import com.cttest.model.entity.Student;
import com.cttest.repository.StudentRepository;
import com.cttest.service.StudentService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentServiceImpl extends BaseServiceImpl<Student, Integer, StudentRepository> implements StudentService {

    public StudentServiceImpl(StudentRepository repository) {
        super(repository, "Student");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Student> search(String query, Pageable pageable) {
        return repository.findAll((root, cq, cb) -> {
            Predicate nameLike = cb.like(cb.lower(root.get("name")), "%" + query.toLowerCase() + "%");
            Predicate idLike = cb.like(root.get("identifier").as(String.class), "%" + query + "%");
            return cb.or(nameLike, idLike);
        }, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> findByStudentGroupId(Integer groupId) {
        return repository.findByStudentGroupIdOrderByNameAsc(groupId);
    }

}
