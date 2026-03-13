package com.cinebee.shared.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import jakarta.servlet.http.HttpServletResponse;

public class TokenCookieUtil {
    private static final int REFRESH_TOKEN_MAX_AGE = 7 * 24 * 60 * 60; // 7 days
    private static final int ACCESS_TOKEN_MAX_AGE = 60 * 60; // 1 hour

    public static void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(REFRESH_TOKEN_MAX_AGE)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(ACCESS_TOKEN_MAX_AGE)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
    }
}

