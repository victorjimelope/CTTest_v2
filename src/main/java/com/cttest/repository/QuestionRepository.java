package com.cttest.repository;

import com.cttest.model.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {

    List<Question> findByTestIdOrderByOrderAsc(Integer testId);

    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.answers WHERE q.test.id = :testId ORDER BY q.order")
    List<Question> findWithAnswersByTestId(@Param("testId") Integer testId);

}
