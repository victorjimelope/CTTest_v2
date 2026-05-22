package com.cttest.model.dto;

public record LoginResponse(
        String token,
        String type,
        Integer userId,
        String username,
        boolean admin) {
}
