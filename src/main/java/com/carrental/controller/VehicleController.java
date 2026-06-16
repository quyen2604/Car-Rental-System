package com.carrental.controller;

import com.carrental.model.entity.Vehicle;
import com.carrental.repository.VehicleRepository;
import com.carrental.service.VehicleService; // <-- Đã import lớp Service chuẩn
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor // <-- Sử dụng lombok tự động tạo Constructor để tiêm các dependency final bên dưới
@CrossOrigin(origins = "*")
public class VehicleController {

    private final VehicleRepository vehicleRepository;
    private final VehicleService vehicleService;
    @GetMapping("/search-vehicle")
    public List<Vehicle> searchVehicle(@RequestParam String city, @RequestParam String type) {
        if ("MOTORBIKE".equalsIgnoreCase(type)) {
            return vehicleRepository.findMotorbikesByCity(city);
        }
        return vehicleRepository.findCarsByCity(city);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable int id) {
        return vehicleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        try {
            return ResponseEntity.ok(vehicleRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createVehicle(@RequestBody Map<String, Object> payload) {
        try {
            // Không còn báo lỗi đỏ ở vehicleService nữa vì đã được tiêm qua Constructor ở trên
            Vehicle savedVehicle = vehicleService.registerVehicle(payload);
            return ResponseEntity.ok(savedVehicle);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi xử lý đăng ký phương tiện: " + e.getMessage());
        }
    }

    // API phục vụ Admin phê duyệt hồ sơ xe
    @PutMapping("/{id}/approval")
    public ResponseEntity<?> updateVehicleApproval(@PathVariable int id, @RequestParam String status) {
        try {
            vehicleService.approveOrRejectVehicle(id, status);
            return ResponseEntity.ok("Xử lý phê duyệt hồ sơ xe thành công!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Trạng thái phê duyệt không hợp lệ: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }
}