package com.cttest.repository;

import com.cttest.model.entity.StudentGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentGroupRepository extends JpaRepository<StudentGroup, Integer>, JpaSpecificationExecutor<StudentGroup> {

    List<StudentGroup> findByCenterIdOrderByAddDateDesc(Integer centerId);

}
