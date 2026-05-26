package com.carrental.controller;

import com.carrental.dto.VehicleSearchRequest;
import com.carrental.entity.Vehicle;
import com.carrental.entity.VehicleStatus;
import com.carrental.entity.VehicleType;
import com.carrental.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    // GET /api/vehicles - Lấy tất cả xe
    @GetMapping
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    // GET /api/vehicles/123 - Lấy xe theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    // GET /api/vehicles/search?vehicleType=CAR&address=Hà Nội
    @GetMapping("/search")
    public ResponseEntity<List<Vehicle>> searchVehicles(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String status) {

        VehicleSearchRequest request = new VehicleSearchRequest();
        
        if (type != null && !type.isBlank()) {
            try {
                request.setVehicleType(VehicleType.valueOf(type.toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Ignore invalid type for now, or could throw bad request
            }
        }
        
        if (location != null && !location.isBlank()) {
            request.setAddress(location);
        }
        
        if (status != null && !status.isBlank()) {
             try {
                request.setStatus(VehicleStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Ignore invalid status for now
            }
        } else {
            // Mặc định chỉ tìm xe có sẵn nếu không truyền status
            request.setStatus(VehicleStatus.AVAILABLE);
        }

        return ResponseEntity.ok(vehicleService.searchVehicles(request));
    }

    // POST /api/vehicles - Thêm xe mới
    @PostMapping
    public ResponseEntity<Vehicle> addVehicle(@Valid @RequestBody Vehicle vehicle) {
        return ResponseEntity.ok(vehicleService.addVehicle(vehicle));
    }

    // PUT /api/vehicles/123 - Cập nhật xe
    @PutMapping("/{id}")
    public ResponseEntity<Vehicle> updateVehicle(@PathVariable Long id,
                                                  @Valid @RequestBody Vehicle vehicle) {
        return ResponseEntity.ok(vehicleService.updateVehicle(id, vehicle));
    }

    // DELETE /api/vehicles/123 - Xóa xe
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}
