package com.cttest.service.impl;

import com.cttest.model.entity.Student;
import com.cttest.exception.ResourceNotFoundException;
import com.cttest.repository.StudentRepository;
import com.cttest.service.StudentService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Student> findAll(Pageable pageable) {
        return studentRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Student> search(String query, Pageable pageable) {
        return studentRepository.findAll((root, cq, cb) -> {
            Predicate nameLike = cb.like(cb.lower(root.get("name")), "%" + query.toLowerCase() + "%");
            Predicate idLike = cb.like(root.get("identifier").as(String.class), "%" + query + "%");
            return cb.or(nameLike, idLike);
        }, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Student findById(Integer id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    @Override
    @Transactional
    public Student save(Student student) {
        return studentRepository.save(student);
    }

    @Override
    @Transactional
    public Student update(Student student) {
        return studentRepository.save(student);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        studentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> findByStudentGroupId(Integer groupId) {
        return studentRepository.findByStudentGroupIdOrderByNameAsc(groupId);
    }

}
