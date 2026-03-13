package com.cinebee.application.service;

import java.util.concurrent.CompletableFuture;

import com.cinebee.presentation.dto.request.LoginRequest;
import com.cinebee.presentation.dto.request.RegisterRequest;
import com.cinebee.presentation.dto.request.ResetPasswordRequest;
import com.cinebee.presentation.dto.request.VerifyOtpRequest;
import com.cinebee.presentation.dto.response.TokenResponse;
import com.cinebee.domain.entity.User;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    User register(RegisterRequest request);
    TokenResponse login(LoginRequest request, HttpServletResponse response);
    TokenResponse refreshToken(String refreshToken, HttpServletResponse response);
    void logout(String accessToken);
    CompletableFuture<Boolean> verifyRecaptcha(String recaptchaToken);
    TokenResponse loginWithGoogleIdToken(String idToken, HttpServletResponse response);

    void forgotPassword(String email);

    String verifyOtp(VerifyOtpRequest request);

    void resetPassword(ResetPasswordRequest request);
}
