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

            // Đóng gói dạng Map để đính kèm popupMessage mà không phá hỏng object User cũ
            java.util.Map<String, Object> responseData = new java.util.HashMap<>();
            responseData.put("userId", user.getUserId());
            responseData.put("email", user.getEmail());
            responseData.put("fullName", user.getFullName());
            responseData.put("active", user.isActive());

            // Xác định vai trò (Role) chính xác của user dựa vào Entity của bạn
            String roleStr = "RENTER";
            if (user instanceof com.carrental.model.entity.Owner) {
                roleStr = "OWNER";
            } else if (user.getEmail().contains("admin")) { // Hoặc logic phân biệt ADMIN của bạn
                roleStr = "ADMIN";
            }
            responseData.put("role", roleStr);

            // Quét thông báo từ 2 lớp Observer riêng biệt
            String popupMessage = null;
            if ("OWNER".equals(roleStr)) {
                popupMessage = com.carrental.observer.OwnerObserver.popNotification(user.getUserId());
            } else if ("ADMIN".equals(roleStr)) {
                popupMessage = com.carrental.observer.AdminObserver.popNotification();
            }
            responseData.put("popupMessage", popupMessage);

            return ResponseEntity.ok(responseData);
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
