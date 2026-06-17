package com.carrental.service;

import com.carrental.model.entity.Vehicle;
import com.carrental.model.enums.VehicleStatus;
import com.carrental.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public boolean checkAvailability(int vehicleId) {
        Vehicle v = vehicleRepository.findById(vehicleId).orElse(null);
        // Giả sử xe có trạng thái AVAILABLE thì mới được đặt
        return v != null && v.getVehicleStatus() == VehicleStatus.AVAILABLE;
    }

    @Transactional
    public void updateVehicleStatus(int vehicleId, VehicleStatus status) {
        Vehicle v = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy xe!"));
        v.setVehicleStatus(status);
        vehicleRepository.save(v);
        System.out.println("Đã cập nhật trạng thái xe " + vehicleId + " thành " + status);
    }

    // Thêm các hàm addVehicle, searchVehicles tương tự dùng vehicleRepository...
}