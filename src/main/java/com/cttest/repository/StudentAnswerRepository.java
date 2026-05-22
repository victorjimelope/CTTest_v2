package com.cttest.repository;

import com.cttest.model.entity.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Integer> {

    List<StudentAnswer> findByStudentTestId(Integer studentTestId);

}
