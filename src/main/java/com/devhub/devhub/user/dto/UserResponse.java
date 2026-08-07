package com.devhub.devhub.user.dto;

import com.devhub.devhub.user.Role;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        Role role,
        String bio,
        String profileImageUrl,
        String country,
        String city
) {
}