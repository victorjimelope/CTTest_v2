package com.cttest.model.dto;

import java.math.BigDecimal;
import java.util.Map;

public record ScoreResult(
        int totalQuestions,
        int correctCount,
        BigDecimal score,
        Map<Integer, Integer> answers
) {}
