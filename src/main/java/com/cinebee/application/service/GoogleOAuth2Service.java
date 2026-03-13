package com.cinebee.application.service;

import com.cinebee.shared.common.Role;
import com.cinebee.infrastructure.config.JwtConfig;
import com.cinebee.presentation.dto.response.TokenResponse;
import com.cinebee.domain.entity.User;
import com.cinebee.shared.exception.ApiException;
import com.cinebee.shared.exception.ErrorCode;
import com.cinebee.infrastructure.persistence.repository.UserRepository;
import com.cinebee.shared.util.TokenCookieUtil;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class GoogleOAuth2Service {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtConfig jwtConfig;
    @Autowired
    private StringRedisTemplate redisTemplate;





// ... (other imports) ...

    public TokenResponse loginWithGoogleIdToken(String idToken, HttpServletResponse httpServletResponse) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(idToken);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            String email = claims.getStringClaim("email");
            String sub = claims.getSubject();
            String name = claims.getStringClaim("name");
            String picture = claims.getStringClaim("picture");

            URL jwksUrl = new URL("https://www.googleapis.com/oauth2/v3/certs");
            JWKSet publicKeys = JWKSet.load(jwksUrl);
            List<JWK> keys = publicKeys.getKeys();
            boolean valid = false;
            for (JWK key : keys) {
                if (key instanceof RSAKey) {
                    RSAPublicKey rsaPublicKey = ((RSAKey) key).toRSAPublicKey();
                    JWSVerifier verifier = new RSASSAVerifier(rsaPublicKey);
                    if (signedJWT.verify(verifier)) {
                        valid = true;
                        break;
                    }
                }
            }
            if (!valid)
                throw new ApiException(ErrorCode.UNAUTHORIZED);

            Optional<User> userOpt = userRepository.findByEmail(email);
            User user;
            if (userOpt.isPresent()) {
                user = userOpt.get();
                if (user.getOauthId() == null)
                    user.setOauthId(sub);
                if (user.getProvider() != User.Provider.GOOGLE)
                    user.setProvider(User.Provider.GOOGLE);
                if (name != null && (user.getFullName() == null || user.getFullName().isEmpty()))
                    user.setFullName(name);
                if (picture != null && (user.getAvatarUrl() == null || user.getAvatarUrl().isEmpty()))
                    user.setAvatarUrl(picture);
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
            } else {
                user = new User();
                user.setUsername(generateGoogleUsername(email));
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                user.setEmail(email);
                user.setFullName(name);
                user.setAvatarUrl(picture);
                user.setProvider(User.Provider.GOOGLE);
                user.setOauthId(sub);
                user.setRole(Role.USER);
                user.setCreatedAt(LocalDateTime.now());
                userRepository.save(user);
            }

            String accessToken = jwtConfig.generateToken(user); // Generate accessToken
            String refreshToken = jwtConfig.generateRefreshToken(user);
            saveRefreshTokenToRedis(refreshToken, user.getUsername(), jwtConfig.getRefreshExpirationMs());

            TokenCookieUtil.setTokenCookies(httpServletResponse, accessToken, refreshToken); // Set cookies

            TokenResponse response = new TokenResponse();
            response.setRole(user.getRole().name());
            response.setUserStatus(user.getUserStatus() != null ? user.getUserStatus().name() : null);
            return response;
        } catch (Exception e) {
            // Log error for debugging
            throw new ApiException(ErrorCode.INTERNAL_ERROR);
        }
    }

    private String generateGoogleUsername(String email) {
        String base = email.split("@")[0];
        String username = base;
        int i = 1;
        while (userRepository.findByUsername(username).isPresent()) {
            username = base + i;
            i++;
        }
        return username;
    }

    private void saveRefreshTokenToRedis(String refreshToken, String username, long ttlMillis) {
        String value = String.format("{\"username\":\"%s\",\"refresh_count\":0}", username);
        String encoded = Base64.getEncoder().encodeToString(value.getBytes());
        redisTemplate.opsForValue().set(refreshToken, encoded, ttlMillis, TimeUnit.MILLISECONDS);
    }
}

