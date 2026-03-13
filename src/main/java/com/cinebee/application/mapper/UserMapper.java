package com.cinebee.application.mapper;

import com.cinebee.domain.entity.User;
import com.cinebee.presentation.dto.response.UserResponse;

public class UserMapper {
    public static UserResponse toUserResponse(User user) {
        if (user == null) return null;
        return new UserResponse(
                user.getId(),

                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getDateOfBirth(),
                user.getAvatarUrl(),
                user.getRole() != null ? user.getRole().name() : null,
                user.getProvider() != null ? user.getProvider().name() : null,
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getUserStatus() != null ? user.getUserStatus().name() : null
        );
    }
}

