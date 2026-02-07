package com.sandwich.ragkg.utils;


import com.sandwich.ragkg.dao.entity.UserDO;
import com.sandwich.ragkg.dao.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.SignatureException;
import java.util.*;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret-key}")
    private String secretKeyBase64; // 这里存的是 Base64 编码后的密钥

    private static final long EXPIRATION_TIME = 3600000; // 1 hour (调整为1小时)
    private static final long REFRESH_TOKEN_EXPIRATION_TIME = 604800000; // 7 days (refresh token有效期)
    private static final long REFRESH_THRESHOLD = 300000; // 5分钟：当剩余时间少于5分钟时开始刷新
    private static final long REFRESH_WINDOW = 600000; // 10分钟：token过期后的宽限期
    
    @Autowired
    private UserMapper userMapper;


    /**
     * 解析 Base64 密钥，并返回 SecretKey
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKeyBase64);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 JWT Token（集成Redis缓存）
     */
    public String generateToken(String username) {
        SecretKey key = getSigningKey(); // 解析密钥
        
        // 获取用户信息
        UserDO user = userMapper.findByUserName(username);
        // 生成唯一的tokenId
        String tokenId = UUID.randomUUID().toString().replace("-", "");;
        long expireTime = System.currentTimeMillis() + EXPIRATION_TIME;
        
        // 创建token内容
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenId", tokenId); // 添加tokenId用于Redis缓存
        claims.put("role", user.getRole());
        claims.put("userId", user.getId().toString()); // 添加用户ID到JWT
        
        // 添加组织标签信息
        if (user.getOrgTags() != null && !user.getOrgTags().isEmpty()) {
            claims.put("orgTags", user.getOrgTags());
        }
        
        // 添加主组织标签信息
        if (user.getPrimaryOrg() != null && !user.getPrimaryOrg().isEmpty()) {
            claims.put("primaryOrg", user.getPrimaryOrg());
        }

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setExpiration(new Date(expireTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

//        // 缓存refresh token信息到Redis
//        tokenCacheService.cacheRefreshToken(refreshTokenId, user.getId().toString(), null, expireTime);

        logger.info("Token generated and cached for user: {}, tokenId: {}", username, tokenId);
        return token;
    }

    /**
     * 验证 JWT Token 是否有效（优先使用Redis缓存）
     */
    public boolean validateToken(String token) {
        try {
            // 首先从JWT中提取tokenId（快速失败）
            String tokenId = extractTokenIdFromToken(token);
            if (tokenId == null) {
                logger.warn("Token does not contain tokenId");
                return false;
            }
            
//            // 检查Redis缓存中的token状态
//            if (!tokenCacheService.isTokenValid(tokenId)) {
//                logger.debug("Token invalid in cache: {}", tokenId);
//                return false;
//            }
            
            // Redis验证通过，再验证JWT签名（双重验证）
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);

            logger.debug("Token validation successful: {}", tokenId);
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("Token expired: {}", e.getClaims().get("tokenId", String.class));
        } catch (Exception e) {
            logger.error("Error validating token", e);
        }
        return false;
    }

    /**
     * 从 JWT Token 中提取tokenId
     */
    public String extractTokenIdFromToken(String token) {
        try {
            Claims claims = extractClaimsIgnoreExpiration(token);
            return claims != null ? claims.get("tokenId", String.class) : null;
        } catch (Exception e) {
            logger.debug("Error extracting tokenId from token", e);
            return null;
        }
    }

    /**
     * 提取Claims，忽略过期异常
     */
    private Claims extractClaimsIgnoreExpiration(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // 忽略过期异常，返回claims
            return e.getClaims();
        } catch (Exception e) {
            logger.debug("Cannot extract claims from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 Token 中提取用户名
     */
    public String extractUsername(String token) {
        try {
            Claims claims = extractClaimsIgnoreExpiration(token);
            return claims != null ? claims.getSubject() : null;
        } catch (Exception e) {
            logger.debug("Error extracting username from token", e);
            return null;
        }
    }

    /**
     * 生成 Refresh Token（长期有效的刷新令牌，集成Redis缓存）
     */
    public String generateRefreshToken(String username) {
        SecretKey key = getSigningKey(); // 解析密钥

        // 获取用户信息
        UserDO user = userMapper.findByUserName(username);
        // 生成唯一的tokenId
        String refreshTokenId = UUID.randomUUID().toString().replace("-", "");
        long expireTime = System.currentTimeMillis() + EXPIRATION_TIME;

        // 创建refreshToken内容（相对简单，只包含基本信息）
        Map<String, Object> claims = new HashMap<>();
        claims.put("refreshTokenId", refreshTokenId); // 添加refreshTokenId
        claims.put("userId", user.getId().toString());
        claims.put("type", "refresh"); // 标识这是一个refresh token

        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setExpiration(new Date(expireTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

//        // 缓存refresh token信息到Redis
//        tokenCacheService.cacheRefreshToken(refreshTokenId, user.getId().toString(), null, expireTime);

        logger.info("Refresh token generated and cached for user: {}, refreshTokenId: {}", username, refreshTokenId);
        return refreshToken;
    }
}
