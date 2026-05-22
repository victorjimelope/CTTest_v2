package com.cttest.model.dto;

import com.cttest.model.enums.TestStatus;
import com.cttest.model.enums.TestType;

import java.util.List;

public record TestDTO(
        Integer id,
        Integer centerId,
        String name,
        String description,
        TestType type,
        TestStatus status,
        List<QuestionDTO> questions) {
}
