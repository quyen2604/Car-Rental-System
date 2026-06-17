package com.carrental.controller;

import com.carrental.DTO.VehicleRequest;
import com.carrental.model.entity.Vehicle;
import com.carrental.repository.VehicleRepository;
import com.carrental.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VehicleController {

    private final VehicleService vehicleService;
    private final VehicleRepository vehicleRepository;

    // Tìm kiếm xe (public)
    @GetMapping("/search-vehicle")
    public ResponseEntity<?> searchVehicle(@RequestParam String city, @RequestParam String type) {
        try {
            List<Vehicle> vehicles;
            if ("MOTORBIKE".equalsIgnoreCase(type)) {
                vehicles = vehicleRepository.findMotorbikesByCity(city);
            } else {
                vehicles = vehicleRepository.findCarsByCity(city);
            }
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Lấy thông tin chi tiết 1 xe (public)
    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable int id) {
        return vehicleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // [OWNER] Đăng ký xe mới
    @PostMapping("/register")
    public ResponseEntity<?> registerVehicle(@RequestBody VehicleRequest request) {
        try {
            Vehicle vehicle = vehicleService.registerVehicle(request);
            return ResponseEntity.ok(vehicle);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // [OWNER] Xem danh sách xe của mình
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<?> getOwnerVehicles(@PathVariable int ownerId) {
        try {
            List<Vehicle> vehicles = vehicleService.getOwnerVehicles(ownerId);
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // [ADMIN] Lấy danh sách xe đang chờ duyệt
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingVehicles() {
        try {
            List<Vehicle> vehicles = vehicleService.getPendingVehicles();
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // [ADMIN] Duyệt hoặc từ chối xe
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveVehicle(@PathVariable int id, @RequestParam String status) {
        try {
            Vehicle vehicle = vehicleService.approveVehicle(id, status);
            return ResponseEntity.ok(vehicle);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

