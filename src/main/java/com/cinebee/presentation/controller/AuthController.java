package com.cinebee.presentation.controller;

import com.cinebee.presentation.dto.request.ForgotPasswordRequest;
import com.cinebee.presentation.dto.request.GoogleTokenRequest;
import com.cinebee.presentation.dto.request.ResetPasswordRequest;
import com.cinebee.presentation.dto.request.VerifyOtpRequest;
import com.cinebee.presentation.dto.response.TokenResponse;
import com.cinebee.application.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> refreshToken(@CookieValue(name = "refreshToken") String refreshToken, HttpServletResponse response) {
        TokenResponse tokenResponse = authService.refreshToken(refreshToken, response);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(name = "accessToken") String accessToken) {
        authService.logout(accessToken);
        // XÃ³a cookie accessToken vÃ  refreshToken phÃ­a client
        return ResponseEntity.ok(new HashMap<String, Object>() {{
            put("message", "Logged out successfully");
        }});
    }

    @PostMapping("/google")
    public ResponseEntity<TokenResponse> loginWithGoogle(@RequestBody GoogleTokenRequest request, HttpServletResponse response) {
        TokenResponse tokenResponse = authService.loginWithGoogleIdToken(request.getIdToken(), response);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(new HashMap<String, Object>() {{
            put("message", "Password reset OTP sent to your email");
        }});
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        String temporaryToken = authService.verifyOtp(request);
        return ResponseEntity.ok(new HashMap<String, Object>() {{
            put("message", "OTP verified successfully");
            put("temporaryToken", temporaryToken);
        }});
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(new HashMap<String, Object>() {{
            put("message", "Password reset successfully");
        }});
    }
}


