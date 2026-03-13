package com.cinebee.presentation.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cinebee.presentation.dto.request.LoginRequest;
import com.cinebee.presentation.dto.response.BaseResponse;
import com.cinebee.presentation.dto.response.TokenResponse;
import com.cinebee.application.service.AuthenticationService;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/auth/login")
public class LoginController {
    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping
    public ResponseEntity<BaseResponse<TokenResponse>> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        TokenResponse tokenResponse = authenticationService.login(request, response);
        return ResponseEntity.ok(BaseResponse.success(tokenResponse, "Login successful"));
    }
}

