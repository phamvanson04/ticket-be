package com.cinebee.application.service;

import com.cinebee.infrastructure.config.JwtConfig;
import com.cinebee.presentation.dto.response.TokenResponse;
import com.cinebee.domain.entity.User;
import com.cinebee.shared.exception.ApiException;
import com.cinebee.shared.exception.ErrorCode;
import com.cinebee.infrastructure.persistence.repository.UserRepository;
import com.cinebee.shared.util.TokenCookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {
    @Autowired
    private JwtConfig jwtConfig;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final int MAX_REFRESH_COUNT = 5;
    private static final String REFRESH_TOKEN_PREFIX = "refresh:";

    public TokenResponse createTokenResponse(User user, int refreshCount, HttpServletResponse httpServletResponse) {
        String accessToken = jwtConfig.generateToken(user);
        String refreshToken = jwtConfig.generateRefreshToken(user);

        saveRefreshTokenWithCount(
            refreshToken,
            user.getUsername(),
            refreshCount,
            jwtConfig.getRefreshExpirationMs()
        );

        TokenCookieUtil.setTokenCookies(httpServletResponse, accessToken, refreshToken);

        TokenResponse response = new TokenResponse();
        response.setRole(user.getRole().name());
        response.setUserStatus(user.getUserStatus() != null ? user.getUserStatus().name() : null);
        return response;
    }

    public TokenResponse refreshToken(String refreshToken, HttpServletResponse httpServletResponse) {
        String redisKey = REFRESH_TOKEN_PREFIX + refreshToken;
        String encoded = redisTemplate.opsForValue().get(redisKey);
        if (encoded == null) {
            throw new ApiException(ErrorCode.TOKEN_INVALID);
        }
        try {
            String value = new String(Base64.getDecoder().decode(encoded));
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(value, Map.class);
            String username = (String) data.get("username");
            int refreshCount = (int) (data.get("refresh_count") instanceof Integer ? data.get("refresh_count") : ((Number)data.get("refresh_count")).intValue());
            if (refreshCount >= MAX_REFRESH_COUNT) {
                redisTemplate.delete(redisKey);
                throw new ApiException(ErrorCode.REFRESH_TOKEN_LIMIT_EXCEEDED);
            }
            User user = userRepository.findByUsername(username).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_EXISTED));
            redisTemplate.delete(redisKey);
            return createTokenResponse(user, refreshCount + 1, httpServletResponse);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR);
        }
    }

    public void blacklistToken(String accessToken, long ttl) {
        redisTemplate.opsForValue().set("blacklist:" + accessToken, "1", ttl, TimeUnit.MILLISECONDS);
    }

    public boolean isTokenBlacklisted(String accessToken) {
        String val = redisTemplate.opsForValue().get("blacklist:" + accessToken);
        return val != null;
    }

    private void saveRefreshTokenWithCount(String refreshToken, String username, int refreshCount, long ttlMillis) {
        try {
            String value = objectMapper.writeValueAsString(Map.of(
                    "username", username,
                    "refresh_count", refreshCount
            ));
            String encoded = Base64.getEncoder().encodeToString(value.getBytes());
            redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + refreshToken, encoded, ttlMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR);
        }
    }
}
