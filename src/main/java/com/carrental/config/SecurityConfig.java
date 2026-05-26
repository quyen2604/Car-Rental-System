package com.carrental.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())   // Tắt CSRF cho đơn giản (dev mode)

            .authorizeHttpRequests(auth -> auth
                // Cho phép truy cập không cần đăng nhập
                .requestMatchers("/", "/index.html", "/login.html", "/search.html").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/api/vehicles/search", "/api/vehicles").permitAll()
                .requestMatchers("/api/auth/register").permitAll()

                // Các request khác phải đăng nhập
                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login.html")           // Trang login tùy chỉnh
                .loginProcessingUrl("/api/auth/login")  // URL xử lý đăng nhập
                .defaultSuccessUrl("/search.html", true)
                .failureUrl("/login.html?error=true")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessUrl("/login.html?logout=true")
                .permitAll()
            );

        return http.build();
    }
}
