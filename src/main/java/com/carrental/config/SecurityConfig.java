package com.carrental.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Kích hoạt cấu hình CORS (Cho phép file HTML bên ngoài gọi vào API)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. Tắt CSRF (Bắt buộc phải tắt đối với REST API sử dụng Stateless)
                .csrf(csrf -> csrf.disable())

                // 3. Cấu hình phân quyền đường dẫn
                .authorizeHttpRequests(auth -> auth
                        // Cho phép truy cập H2 Console
                        .requestMatchers("/h2-console/**").permitAll()
                        // Mở cửa hoàn toàn cho các API liên quan đến Authentication (Đăng ký, Đăng
                        // nhập)
                        .requestMatchers("/api/auth/**").permitAll()
                        // Bất kỳ request nào khác tạm thời mở ra để chúng ta dễ test giao diện gốc
                        .anyRequest().permitAll())

                // Cho phép hiển thị iframe cho H2 console
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                // 4. Đưa Session về chế độ STATELESS (Không dùng Session dính liền giao diện
                // nữa)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    // Bộ cấu hình CORS cấp quyền cho Frontend kết nối mượt mà
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*")); // Thay đổi thành AllowedOriginPatterns
        configuration.setAllowedMethods(List.of("*")); // Cho phép mọi method
        configuration.setAllowedHeaders(List.of("*")); // Cho phép mọi header
        configuration.setAllowCredentials(true); // Nếu frontend có dùng cookie/session (optional)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}