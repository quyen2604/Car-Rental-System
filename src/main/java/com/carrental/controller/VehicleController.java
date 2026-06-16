package com.carrental.controller;

import com.carrental.model.entity.Car;
import com.carrental.model.entity.Motorbike;
import com.carrental.model.entity.Vehicle;
import com.carrental.model.enums.VehicleStatus;
import com.carrental.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VehicleController {

    @Autowired
    private VehicleRepository vehicleRepository;

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
            List<Vehicle> list = vehicleRepository.findAll();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createVehicle(@RequestBody java.util.Map<String, Object> payload) {
        try {
            Vehicle vehicle;

            if (payload.containsKey("seatNumber") && payload.get("seatNumber") != null && !payload.get("seatNumber").toString().isEmpty()) {
                Car car = new Car();
                car.setSeatNumber(Integer.parseInt(payload.get("seatNumber").toString()));
                vehicle = car;
            }
            else {
                Motorbike motorbike = new Motorbike();
                if (payload.containsKey("engineCapacity") && payload.get("engineCapacity") != null && !payload.get("engineCapacity").toString().isEmpty()) {
                    motorbike.setEngineCapacity(Integer.parseInt(payload.get("engineCapacity").toString()));
                }
                vehicle = motorbike;
            }

            vehicle.setBrand(payload.get("brand").toString());
            vehicle.setModel(payload.get("model").toString());
            vehicle.setLicensePlate(payload.get("licensePlate").toString());
            vehicle.setPricePerDay(Double.parseDouble(payload.get("pricePerDay").toString()));
            vehicle.setDescription(payload.get("description") != null ? payload.get("description").toString() : "");

            vehicle.setVehicleStatus(VehicleStatus.PENDING);

            Vehicle savedVehicle = vehicleRepository.save(vehicle);
            return ResponseEntity.ok(savedVehicle);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi xử lý đăng ký phương tiện: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateVehicleStatus(@PathVariable int id, @RequestParam String status) {
        try {
            Vehicle vehicle = vehicleRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phương tiện với ID: " + id));

           VehicleStatus statusEnum = VehicleStatus.valueOf(status.toUpperCase().trim());
            vehicle.setVehicleStatus(statusEnum);

            vehicleRepository.save(vehicle);

            return ResponseEntity.ok("Thay đổi trạng thái duyệt xe thành công!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Trạng thái duyệt xe không hợp lệ: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }
}