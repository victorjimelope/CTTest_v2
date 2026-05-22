package com.cttest.model.dto;

import jakarta.validation.constraints.NotNull;

public record PinRequest(
        @NotNull Integer pin,
        @NotNull Integer studentId) {
}
