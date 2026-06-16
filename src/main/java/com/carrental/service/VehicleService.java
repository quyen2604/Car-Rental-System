package com.carrental.service;

import com.carrental.model.entity.Admin;
import com.carrental.model.entity.Car;
import com.carrental.model.entity.Motorbike;
import com.carrental.model.entity.User;
import com.carrental.model.entity.Vehicle;
import com.carrental.model.enums.ApprovalStatus;
import com.carrental.model.enums.VehicleStatus;
import com.carrental.notification.NotificationEvent;
import com.carrental.notification.NotificationSubject;
import com.carrental.repository.UserRepository;
import com.carrental.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final NotificationSubject notificationSubject;

    // --- CHUYỂN TOÀN BỘ LOGIC ĐĂNG KÝ TỪ CONTROLLER SANG SERVICE ---
    @Transactional
    public Vehicle registerVehicle(Map<String, Object> payload) {
        Vehicle vehicle;

        // Phân loại hình học thực thể Car hoặc Motorbike
        if (payload.containsKey("seatNumber") && payload.get("seatNumber") != null && !payload.get("seatNumber").toString().isEmpty()) {
            Car car = new Car();
            car.setSeatNumber(Integer.parseInt(payload.get("seatNumber").toString()));
            vehicle = car;
        } else {
            Motorbike motorbike = new Motorbike();
            if (payload.containsKey("engineCapacity") && payload.get("engineCapacity") != null && !payload.get("engineCapacity").toString().isEmpty()) {
                motorbike.setEngineCapacity(Integer.parseInt(payload.get("engineCapacity").toString()));
            }
            vehicle = motorbike;
        }

        // Gán thông tin cơ bản
        vehicle.setBrand(payload.get("brand").toString());
        vehicle.setModel(payload.get("model").toString());
        vehicle.setLicensePlate(payload.get("licensePlate").toString());
        vehicle.setPricePerDay(Double.parseDouble(payload.get("pricePerDay").toString()));
        vehicle.setDescription(payload.get("description") != null ? payload.get("description").toString() : "");

        //Đăng ký mới thì approvalStatus = PENDING
        vehicle.setApprovalStatus(ApprovalStatus.PENDING);
        vehicle.setVehicleStatus(VehicleStatus.MAINTENANCE); // Mặc định xe chưa được lên sàn thương mại khi chưa duyệt

        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        // Gửi thông báo đến Admin hệ thống qua Observer của bạn
        try {
            List<User> users = userRepository.findAll();
            int adminId = users.stream()
                    .filter(u -> u instanceof Admin)
                    .map(User::getUserId)
                    .findFirst()
                    .orElse(1);

            String msg = String.format("Có yêu cầu duyệt xe mới: %s %s (Biển số: %s). Vui lòng phê duyệt!",
                    savedVehicle.getBrand(), savedVehicle.getModel(), savedVehicle.getLicensePlate());
            notificationSubject.notifyObservers(new NotificationEvent("VEHICLE_REGISTER", msg, adminId));
        } catch (Exception e) {
            // Không để lỗi gửi thông báo làm sập quá trình lưu DB
            System.err.println("Lỗi gửi thông báo: " + e.getMessage());
        }

        return savedVehicle;
    }

    @Transactional
    public void approveOrRejectVehicle(int vehicleId, String statusStr) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phương tiện với ID: " + vehicleId));

        ApprovalStatus approval = ApprovalStatus.valueOf(statusStr.toUpperCase().trim());
        vehicle.setApprovalStatus(approval);

        if (approval == ApprovalStatus.APPROVED) {
            // Nếu hồ sơ ĐƯỢC DUYỆT -> Cho phép xe lên sàn, trạng thái vận hành chuyển sang AVAILABLE
            vehicle.setVehicleStatus(VehicleStatus.AVAILABLE);
        } else if (approval == ApprovalStatus.REJECTED) {
            // Nếu hồ sơ BỊ TỪ CHỐI -> Giữ nguyên trạng thái vận hành là MAINTENANCE (hoặc để ẩn khỏi trang chủ)
            // Vì trong VehicleStatus không có REJECTED, việc đưa về MAINTENANCE là giải pháp an toàn và hợp lý nhất
            vehicle.setVehicleStatus(VehicleStatus.MAINTENANCE);
        }

        vehicleRepository.save(vehicle);
    }
}