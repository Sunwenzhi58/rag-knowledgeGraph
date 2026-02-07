package com.sandwich.ragkg.utils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户上下文工具类，用于从请求中获取当前登录用户信息
 *
 * @author sunwenzhi
 * @date 2026/2/7
 */
public class UserContextUtil {

    private static final String TOKEN_HEADER = "token";

    /**
     * 从请求中获取 token
     */
    public static String getToken(HttpServletRequest request) {
        return request.getHeader(TOKEN_HEADER);
    }

    /**
     * 从 token 中提取用户名
     */
    public static String getUsername(HttpServletRequest request, JwtUtils jwtUtils) {
        String token = getToken(request);
        if (token == null || token.isEmpty()) {
            return null;
        }
        return jwtUtils.extractUsername(token);
    }
}
