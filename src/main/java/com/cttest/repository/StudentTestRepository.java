package com.cttest.repository;

import com.cttest.model.entity.StudentTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentTestRepository extends JpaRepository<StudentTest, Integer> {

    List<StudentTest> findByTestInstanceId(Integer testInstanceId);

    Optional<StudentTest> findByTestInstanceIdAndStudentId(Integer testInstanceId, Integer studentId);

    List<StudentTest> findByStudentId(Integer studentId);

}
