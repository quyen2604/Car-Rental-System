package com.carrental.service;

import com.carrental.entity.User;
import com.carrental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Spring Security gọi method này để xác thực user
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User không tồn tại: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    // Đăng ký user mới
    public User registerUser(String email, String password, String fullName) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email đã tồn tại!");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))  // Mã hóa password
                .role(com.carrental.entity.Role.RENTER)
                .fullName(fullName)
                .build();

        return userRepository.save(user);
    }

    // Tìm kiếm user theo email
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    // Tự động tìm hoặc tạo tài khoản khách (Guest)
    public User getOrCreateGuestUser(String email, String fullName, String phone) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User guest = User.builder()
                    .email(email)
                    .fullName(fullName)
                    .phone(phone)
                    .password(passwordEncoder.encode(java.util.UUID.randomUUID().toString()))
                    .role(com.carrental.entity.Role.RENTER)
                    .status(com.carrental.entity.UserStatus.ACTIVE)
                    .build();
            return userRepository.save(guest);
        });
    }
}
