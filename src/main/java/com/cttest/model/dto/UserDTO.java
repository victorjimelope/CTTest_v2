package com.cttest.model.dto;

import java.util.Set;

public record UserDTO(
        Integer id,
        String username,
        boolean admin,
        Integer centerId,
        String centerName,
        Set<Integer> studentGroupIds) {
}
