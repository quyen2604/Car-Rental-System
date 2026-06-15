package com.carrental.controller;

import com.carrental.DTO.LoginRequest;
import com.carrental.model.entity.Renter;
import com.carrental.model.entity.User;
import com.carrental.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Cho phép Frontend gọi API không bị lỗi CORS
public class AuthController {

    @Autowired
    private AuthService authService;

    // API Đăng ký dành cho Người Thuê Xe (Renter)
    @PostMapping("/register/renter")
    public ResponseEntity<?> registerRenter(@RequestBody Renter renter) {
        try {
            User registeredUser = authService.register(renter);
            return ResponseEntity.ok(registeredUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API Đăng nhập chung cho hệ thống
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            User user = authService.login(loginRequest);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API Đăng xuất
    @PostMapping("/logout/{id}")
    public ResponseEntity<?> logout(@PathVariable int id) {
        authService.logout(id);
        return ResponseEntity.ok("Đăng xuất thành công!");
    }


}
