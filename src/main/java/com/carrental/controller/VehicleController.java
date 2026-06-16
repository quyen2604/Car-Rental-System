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
@CrossOrigin(origins = "*") // 🚀 Thêm dòng này để Frontend gọi API không bị lỗi CORS block
public class VehicleController {

    @Autowired
    private VehicleRepository vehicleRepository;

    // 1. Tìm kiếm xe (Giữ nguyên code cũ của bạn)
    @GetMapping("/search-vehicle")
    public List<Vehicle> searchVehicle(@RequestParam String city, @RequestParam String type) {
        if ("MOTORBIKE".equalsIgnoreCase(type)) {
            return vehicleRepository.findMotorbikesByCity(city);
        }
        return vehicleRepository.findCarsByCity(city);
    }

    // 2. Lấy thông tin chi tiết của 1 xe (Giữ nguyên code cũ của bạn)
    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable int id) {
        return vehicleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. LẤY TẤT CẢ XE (Phục vụ cho Admin Quét và Lọc xe trạng thái PENDING)
    @GetMapping
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        try {
            List<Vehicle> list = vehicleRepository.findAll();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 4. API OWNER ĐĂNG KÝ XE MỚI (Tự động nhận diện lưu vào Car hoặc Motorbike)
    @PostMapping
    public ResponseEntity<?> createVehicle(@RequestBody java.util.Map<String, Object> payload) {
        try {
            Vehicle vehicle;

            // Kiểm tra nếu có trường seatNumber (Số chỗ) -> Đây là Ô tô (Car)
            if (payload.containsKey("seatNumber") && payload.get("seatNumber") != null && !payload.get("seatNumber").toString().isEmpty()) {
                Car car = new Car();
                car.setSeatNumber(Integer.parseInt(payload.get("seatNumber").toString()));
                vehicle = car;
            }
            // Nếu không thì khởi tạo thực thể là Xe máy (Motorbike)
            else {
                Motorbike motorbike = new Motorbike();
                // ✅ SỬA LỖI: Ép từ payload sang kiểu số nguyên (int) cho dung tích xe máy
                if (payload.containsKey("engineCapacity") && payload.get("engineCapacity") != null && !payload.get("engineCapacity").toString().isEmpty()) {
                    motorbike.setEngineCapacity(Integer.parseInt(payload.get("engineCapacity").toString()));
                }
                vehicle = motorbike;
            }

            // Gán các thuộc tính chung kế thừa từ lớp cha Vehicle
            vehicle.setBrand(payload.get("brand").toString());
            vehicle.setModel(payload.get("model").toString());
            vehicle.setLicensePlate(payload.get("licensePlate").toString());
            vehicle.setPricePerDay(Double.parseDouble(payload.get("pricePerDay").toString()));
            vehicle.setDescription(payload.get("description") != null ? payload.get("description").toString() : "");

            // ✅ SỬA LỖI: Gán trực tiếp trạng thái Enum PENDING vừa thêm ở bước 1
            vehicle.setVehicleStatus(VehicleStatus.PENDING);

            Vehicle savedVehicle = vehicleRepository.save(vehicle);
            return ResponseEntity.ok(savedVehicle);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi xử lý đăng ký phương tiện: " + e.getMessage());
        }
    }

    // 5. API ADMIN PHÊ DUYỆT THAY ĐỔI TRẠNG THÁI XE (AVAILABLE / REJECTED)
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateVehicleStatus(@PathVariable int id, @RequestParam String status) {
        try {
            Vehicle vehicle = vehicleRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phương tiện với ID: " + id));

            // ✅ SỬA LỖI: Ép chuỗi String (AVAILABLE / REJECTED) từ Frontend gửi lên thành Enum VehicleStatus
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