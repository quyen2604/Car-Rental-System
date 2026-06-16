package com.carrental.service;

import com.carrental.model.entity.Admin;
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

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final NotificationSubject notificationSubject;

    @Transactional
    public Vehicle registerVehicle(Vehicle vehicle) {
        // Thiết lập trạng thái ban đầu của xe đăng ký mới
        vehicle.setVehicleStatus(VehicleStatus.AVAILABLE);
        vehicle.setApprovalStatus(ApprovalStatus.PENDING);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        // Lấy danh sách tài khoản Admin để tìm ID người nhận thông báo duyệt xe
        List<User> users = userRepository.findAll();
        int adminId = users.stream()
                .filter(u -> u instanceof Admin)
                .map(User::getUserId)
                .findFirst()
                .orElse(1); // Mặc định ID nếu hệ thống chưa khởi tạo kịp admin dữ liệu

        String msg = String.format("Chủ xe %s đã đăng ký xe mới: %s %s (Biển số: %s). Vui lòng phê duyệt ngay!",
                vehicle.getOwner().getFullName(), vehicle.getBrand(), vehicle.getModel(), vehicle.getLicensePlate());

        notificationSubject.notifyObservers(new NotificationEvent("VEHICLE_REGISTER", msg, adminId));

        return savedVehicle;
    }
}