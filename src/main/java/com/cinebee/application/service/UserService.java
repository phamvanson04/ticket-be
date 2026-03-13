package com.cinebee.application.service;

import com.cinebee.presentation.dto.response.UserResponse;

public interface UserService {
    UserResponse getUserProfile(String username);
}

