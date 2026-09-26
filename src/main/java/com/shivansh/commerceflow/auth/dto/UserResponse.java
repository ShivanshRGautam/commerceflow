package com.shivansh.commerceflow.auth.dto;

import com.shivansh.commerceflow.user.Role;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        boolean active,
        LocalDateTime createdAt
) {
}
