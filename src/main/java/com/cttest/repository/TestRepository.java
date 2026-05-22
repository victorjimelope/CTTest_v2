package com.cttest.repository;

import com.cttest.model.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestRepository extends JpaRepository<Test, Integer>, JpaSpecificationExecutor<Test> {

    List<Test> findByCenterIdOrderByAddDateDesc(Integer centerId);

}
