package com.carrental.service;

import com.carrental.DTO.LoginRequest;
import com.carrental.model.entity.User;
import com.carrental.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    public User register(User user) {
        // Kiểm tra xem email đã tồn tại chưa
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("Email này đã được sử dụng!");
        }
        user.setActive(true); // Mặc định kích hoạt tài khoản
        return userRepository.save(user);
    }

    public User login(LoginRequest loginRequest) {
        // Tìm user theo email
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại!"));

        // Kiểm tra mật khẩu
        if (!user.getPassword().equals(loginRequest.getPassword())) {
            throw new RuntimeException("Mật khẩu không chính xác!");
        }

        if (!user.isActive()) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa!");
        }

        return user;
    }

    public void logout(int userId) {
        System.out.println("User với ID " + userId + " đã đăng xuất.");
    }
}