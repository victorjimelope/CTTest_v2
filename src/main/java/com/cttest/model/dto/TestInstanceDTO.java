package com.cttest.model.dto;

import com.cttest.model.enums.TestInstanceStatus;

import java.time.LocalDate;

public record TestInstanceDTO(
        Integer id,
        Integer testId,
        String testName,
        Integer studentGroupId,
        String studentGroupName,
        Integer pin,
        TestInstanceStatus status,
        LocalDate assignedDate,
        Integer assignedByUserId,
        String assignedByUsername) {
}
