package com.carrental.controller;

import com.carrental.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        String fullName = body.get("fullName");

        userService.registerUser(email, password, fullName);
        return ResponseEntity.ok(Map.of("message", "Đăng ký thành công!"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(java.security.Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }
        com.carrental.entity.User user = userService.findByEmail(principal.getName());
        if (user == null) {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }
        return ResponseEntity.ok(Map.of(
            "authenticated", true,
            "id", user.getId(),
            "email", user.getEmail(),
            "fullName", user.getFullName(),
            "phone", user.getPhone() != null ? user.getPhone() : ""
        ));
    }
}
