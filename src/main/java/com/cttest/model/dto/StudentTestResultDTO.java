package com.cttest.model.dto;

import java.math.BigDecimal;

public record StudentTestResultDTO(
        Integer studentTestId,
        String studentName,
        String testName,
        BigDecimal score,
        int correctCount,
        int totalQuestions
) {}
