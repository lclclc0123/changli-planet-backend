package com.creamakers.websystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/web/users/login","/web/users/{userId}/groups","/web/users/me","/web/users/me/token",
                                "/web/violations","/web/users/{userId}/violations","/web/violations","/web/violations/{violationId}",
                                "/web/violation/search","/web/users/{userId}/violations/statistics","/web/violations/statistics","/web/violations/statistics",
                                "/web/users/{userId}/permissions","/web/groups/{groupId}/messages","/web/posts","/web/posts/{post_id}","/web/users/{userId}",
                                "/web/users","/web/permissions","/web/posts/reported","/web/posts/{post_id}/comments"
                        ).permitAll()  // 允许注册和登录端点
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .formLogin(withDefaults())
                .httpBasic(withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
