package com.cttest.model.dto;

import jakarta.validation.constraints.NotNull;

public record AnswerRequest(
        @NotNull Integer questionId,
        @NotNull Integer answerId) {
}
