package com.karma.platform.model;

public record User(
        String id,
        String email,
        String passwordHash,
        String firstName,
        String lastName,
        String avatarUrl,
        String bio,
        String phone,
        UserRole role,
        String locale,
        boolean emailVerified
) {
}
