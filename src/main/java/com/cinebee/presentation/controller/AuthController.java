package com.cinebee.presentation.controller;

import com.cinebee.presentation.dto.request.ForgotPasswordRequest;
import com.cinebee.presentation.dto.request.GoogleTokenRequest;
import com.cinebee.presentation.dto.request.ResetPasswordRequest;
import com.cinebee.presentation.dto.request.VerifyOtpRequest;
import com.cinebee.presentation.dto.response.BaseResponse;
import com.cinebee.presentation.dto.response.TokenResponse;
import com.cinebee.application.service.AuthenticationService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/refresh-token")
    public ResponseEntity<BaseResponse<TokenResponse>> refreshToken(@CookieValue(name = "refreshToken") String refreshToken, HttpServletResponse response) {
        TokenResponse tokenResponse = authenticationService.refreshToken(refreshToken, response);
        return ResponseEntity.ok(BaseResponse.success(tokenResponse, "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Map<String, String>>> logout(@CookieValue(name = "accessToken") String accessToken) {
        authenticationService.logout(accessToken);
        Map<String, String> payload = Map.of("message", "Logged out successfully");
        return ResponseEntity.ok(BaseResponse.success(payload, "Logout successful"));
    }

    @PostMapping("/google")
    public ResponseEntity<BaseResponse<TokenResponse>> loginWithGoogle(@RequestBody GoogleTokenRequest request, HttpServletResponse response) {
        TokenResponse tokenResponse = authenticationService.loginWithGoogleIdToken(request.getIdToken(), response);
        return ResponseEntity.ok(BaseResponse.success(tokenResponse, "Google login successful"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<BaseResponse<Map<String, String>>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authenticationService.forgotPassword(request.getEmail());
        Map<String, String> payload = Map.of("message", "Password reset OTP sent to your email");
        return ResponseEntity.ok(BaseResponse.success(payload, "OTP sent"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<BaseResponse<Map<String, String>>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        String temporaryToken = authenticationService.verifyOtp(request);
        Map<String, String> payload = Map.of(
                "message", "OTP verified successfully",
                "temporaryToken", temporaryToken);
        return ResponseEntity.ok(BaseResponse.success(payload, "OTP verified"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<BaseResponse<Map<String, String>>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
        Map<String, String> payload = Map.of("message", "Password reset successfully");
        return ResponseEntity.ok(BaseResponse.success(payload, "Password reset successful"));
    }
}


