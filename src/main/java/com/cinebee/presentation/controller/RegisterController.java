package com.cinebee.presentation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cinebee.presentation.dto.request.RegisterRequest;
import com.cinebee.presentation.dto.response.BaseResponse;
import com.cinebee.presentation.dto.response.UserResponse;
import com.cinebee.domain.entity.User;
import com.cinebee.application.mapper.UserMapper;
import com.cinebee.application.service.AuthenticationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth/register")
public class RegisterController {
    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping
    public ResponseEntity<BaseResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        User user = authenticationService.register(request);
        return ResponseEntity.ok(BaseResponse.success(UserMapper.toUserResponse(user), "Register successful"));
    }
}

