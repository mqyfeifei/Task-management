package com.task.backend.config;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. 密码加密器（正确，必须保留）
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. 安全过滤器链（核心配置）
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
                // 配置 CORS（使用内部集成方式，指定配置源）
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 关闭 CSRF（前后端分离场景下正确）
                .csrf(csrf -> csrf.disable())
                // 接口权限控制（正确，放行登录/注册接口）

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/user/register", "/api/user/login").permitAll() // 公开接口
                        .anyRequest().authenticated() // 其他接口需要认证
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class); // 添加 JWT 拦截器
        return http.build();
    }


    // 3. CORS 配置源（仅定义配置，不单独注册为 Filter）
    private UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许前端的源（你的前端地址是 http://localhost:5173，正确）
        config.addAllowedOrigin("http://localhost:3000/");
        // 允许所有请求方法（GET/POST/PUT/DELETE 等，正确）
        config.addAllowedMethod("*");
        // 允许所有请求头（包括自定义头，如 Token，正确）
        config.addAllowedHeader("*");
        // 允许携带 Cookie（如果前端需要传递认证信息，正确）
        config.setAllowCredentials(true);
        // 预检请求的缓存时间（减少OPTIONS请求次数，可选优化）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径生效（正确）
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // 注意：删除单独的 CorsFilter Bean 定义，避免冲突
}

