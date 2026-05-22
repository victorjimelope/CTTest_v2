package com.cttest.service.impl;

import com.cttest.model.entity.StudentTest;
import com.cttest.repository.StudentTestRepository;
import com.cttest.service.StudentTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentTestServiceImpl implements StudentTestService {

    private final StudentTestRepository studentTestRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StudentTest> findAll() {
        return studentTestRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentTest findById(Integer id) {
        return studentTestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("StudentTest not found with id: " + id));
    }

    @Override
    @Transactional
    public StudentTest save(StudentTest studentTest) {
        return studentTestRepository.save(studentTest);
    }

    @Override
    @Transactional
    public StudentTest update(StudentTest studentTest) {
        return studentTestRepository.save(studentTest);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        studentTestRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentTest> findByTestInstanceId(Integer testInstanceId) {
        return studentTestRepository.findByTestInstanceId(testInstanceId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StudentTest> findByTestInstanceAndStudent(Integer testInstanceId, Integer studentId) {
        return studentTestRepository.findByTestInstanceIdAndStudentId(testInstanceId, studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentTest> findByStudentId(Integer studentId) {
        return studentTestRepository.findByStudentId(studentId);
    }

}
