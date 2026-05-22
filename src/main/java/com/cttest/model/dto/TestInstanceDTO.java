package com.cttest.model.dto;

import java.time.LocalDate;

public record TestInstanceDTO(
        Integer id,
        Integer testId,
        String testName,
        Integer studentGroupId,
        String studentGroupName,
        Integer pin,
        LocalDate assignedDate,
        Integer assignedByUserId) {
}
