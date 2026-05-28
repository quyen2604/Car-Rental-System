package com.carrental.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*")); // Cho phép truy cập từ mọi nguồn (bao gồm cả file:// khi mở html trực tiếp)
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())   // Tắt CSRF cho đơn giản (dev mode)

            .authorizeHttpRequests(auth -> auth
                // Cho phép truy cập không cần đăng nhập
                .requestMatchers("/", "/index.html", "/login.html", "/search.html", "/booking.html").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/api/vehicles/**").permitAll() // Cho phép xem danh sách và chi tiết xe
                .requestMatchers("/api/auth/register", "/api/auth/me").permitAll()
                .requestMatchers("/api/bookings/**").permitAll()

                // Các request khác phải đăng nhập
                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login.html")           // Trang login tùy chỉnh
                .loginProcessingUrl("/api/auth/login")  // URL xử lý đăng nhập
                .defaultSuccessUrl("/search.html", true)
                .failureUrl("/login.html?error")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessUrl("/login.html?logout")
                .permitAll()
            );

        return http.build();
    }
}
