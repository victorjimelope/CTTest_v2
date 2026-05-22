package com.cttest.repository;

import com.cttest.model.entity.TestInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestInstanceRepository extends JpaRepository<TestInstance, Integer>, JpaSpecificationExecutor<TestInstance> {

    List<TestInstance> findByTestId(Integer testId);

    Optional<TestInstance> findByPin(Integer pin);

    @Query("SELECT ti.id FROM TestInstance ti WHERE ti.pin = :pin")
    Optional<Integer> getIdByPin(@Param("pin") Integer pin);

}
