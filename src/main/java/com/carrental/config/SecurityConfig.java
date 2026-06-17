//package com.carrental.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import java.util.Arrays;
//import java.util.List;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                // 1. Kích hoạt cấu hình CORS (Cho phép file HTML bên ngoài gọi vào API)
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//
//                // 2. Tắt CSRF (Bắt buộc phải tắt đối với REST API sử dụng Stateless)
//                .csrf(csrf -> csrf.disable())
//
//                // 3. Cấu hình phân quyền đường dẫn
//                .authorizeHttpRequests(auth -> auth
//                        // Mở cửa hoàn toàn cho các API liên quan đến Authentication (Đăng ký, Đăng
//                        // nhập)
//                        .requestMatchers("/api/auth/**").permitAll()
//                        // Bất kỳ request nào khác tạm thời mở ra để chúng ta dễ test giao diện gốc
//                        .anyRequest().permitAll())
//
//                // 4. Đưa Session về chế độ STATELESS (Không dùng Session dính liền giao diện
//                // nữa)
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
//
//        return http.build();
//    }
//
//    // Bộ cấu hình CORS cấp quyền cho Frontend kết nối mượt mà
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(List.of("*")); // Cho phép mọi nguồn (bao gồm file HTML của bạn)
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cache-Control"));
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
//}