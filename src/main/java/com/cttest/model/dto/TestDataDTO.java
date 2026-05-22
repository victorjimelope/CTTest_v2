package com.cttest.model.dto;

import java.util.List;

public record TestDataDTO(
        Integer testId,
        String testName,
        List<QuestionDataDTO> questions,
        int totalQuestions
) {}
