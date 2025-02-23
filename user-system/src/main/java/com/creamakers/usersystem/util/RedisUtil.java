package com.creamakers.usersystem.util;

import com.creamakers.usersystem.service.impl.UserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Value("${REFRESH_TOKEN_PREFIX}")
    private String refreshTokenPrefix;

    @Value("${BLACKLIST_TOKEN_PREFIX}")
    private String blackListTokenPrefix;

    @Value("${JWT_REFRESH_TOKEN_EXPIRATION_TIME}")
    private Long jwtRefreshTokenExpirationTime;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void storeRefreshToken(String username, String deviceId, String refreshToken) {
        String key = refreshTokenPrefix + username + "-" + deviceId;
        redisTemplate.opsForValue().set(key, refreshToken, jwtRefreshTokenExpirationTime, TimeUnit.MILLISECONDS);
        logger.info("Stored refresh token for user {} on device {} with key {}", username, deviceId, key);
    }

    public boolean isRefreshTokenExpired(String username, String deviceId) {
        String key = refreshTokenPrefix + username + "-" + deviceId;
        boolean isExpired = redisTemplate.opsForValue().get(key) == null;
        logger.info("Checked expiration for user {} on device {}: {}", username, deviceId, isExpired);
        return isExpired;
    }


    public void deleteRefreshToken(String username, String deviceId) {
        String key = refreshTokenPrefix + username + "-" + deviceId;
        redisTemplate.delete(key);
        logger.info("Deleted refresh token for user {} on device {} with key {}", username, deviceId, key);
    }

    public boolean refreshTokenExists(String username, String deviceId) {
        String key = refreshTokenPrefix + username + "-" + deviceId;
        boolean exists = redisTemplate.hasKey(key);
        logger.info("Checked existence of refresh token for user {} on device {}: {}", username, deviceId, exists);
        return exists;
    }

    public void addAccessToBlacklist(String accessToken) {
        String key = blackListTokenPrefix + accessToken;
        redisTemplate.opsForSet().add(key, accessToken);
        logger.info("Added access token to blacklist: {}", accessToken);
    }

    public String getCachedAccessTokenFromBlack(String accessToken) {
        String key = blackListTokenPrefix + accessToken;
        Set<String> tokens = redisTemplate.opsForSet().members(key);
        String cachedToken = (tokens != null && !tokens.isEmpty()) ? tokens.iterator().next() : null;
        logger.info("Retrieved cached access token from blacklist for token {}: {}", accessToken, cachedToken);
        return cachedToken;
    }

    public String getFreshTokenByUsernameAndDeviceId(String username, String deviceId) {
        String key = refreshTokenPrefix + username + "-" + deviceId;
        String refreshToken = redisTemplate.opsForValue().get(key);
        if (refreshToken != null) {
            logger.info("Successfully retrieved refresh token for user {} on device {}", username, deviceId);
        } else {
            logger.warn("No refresh token found for user {} on device {}", username, deviceId);
        }
        return refreshToken;
    }
}
