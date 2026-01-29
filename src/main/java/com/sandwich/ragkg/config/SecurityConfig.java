package com.sandwich.ragkg.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author sunwenzhi
 * @description Spring Security配置类
 * @date 2026/1/24
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（因为使用JWT，不需要CSRF保护）
            .csrf(csrf -> csrf.disable())
            // 配置请求授权
            .authorizeHttpRequests(auth -> auth
                // 允许匿名访问的端点
                .requestMatchers("/api/rag-kg/user/register").permitAll()
                .requestMatchers("/api/rag-kg/user/login").permitAll()
                .requestMatchers("/api/rag-kg/user/test1").permitAll()
                // 其他所有请求都需要认证（后续可以添加JWT过滤器）
                .anyRequest().authenticated()
            )
            // 禁用Session（使用JWT，不需要Session）
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // 禁用默认的登录页面重定向
            .formLogin(form -> form.disable())
            // 禁用HTTP Basic认证
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}

