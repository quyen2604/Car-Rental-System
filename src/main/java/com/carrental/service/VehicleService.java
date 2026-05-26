package com.carrental.service;

import com.carrental.dto.VehicleSearchRequest;
import com.carrental.entity.Vehicle;
import com.carrental.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    // Lấy tất cả xe
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    // Tìm xe theo ID
    public Vehicle getVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy xe với ID: " + id));
    }

    // Tìm kiếm xe linh hoạt
    public List<Vehicle> searchVehicles(VehicleSearchRequest request) {
        return vehicleRepository.searchVehicles(
            request.getVehicleType(),
            request.getAddress(),
            request.getStatus()
        );
    }

    // Thêm xe mới
    public Vehicle addVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    // Cập nhật xe (Lưu ý: Logic này sẽ phức tạp hơn nếu phải update Car/Motorbike riêng, đây là bản đơn giản cho Vehicle)
    public Vehicle updateVehicle(Long id, Vehicle vehicleDetails) {
        Vehicle vehicle = getVehicleById(id);
        vehicle.setVehicleType(vehicleDetails.getVehicleType());
        vehicle.setBrand(vehicleDetails.getBrand());
        vehicle.setModel(vehicleDetails.getModel());
        vehicle.setLicensePlate(vehicleDetails.getLicensePlate());
        vehicle.setAddress(vehicleDetails.getAddress());
        vehicle.setPricePerDay(vehicleDetails.getPricePerDay());
        vehicle.setDepositAmount(vehicleDetails.getDepositAmount());
        vehicle.setImageUrl(vehicleDetails.getImageUrl());
        vehicle.setDescription(vehicleDetails.getDescription());
        vehicle.setStatus(vehicleDetails.getStatus());
        return vehicleRepository.save(vehicle);
    }

    // Xóa xe
    public void deleteVehicle(Long id) {
        vehicleRepository.deleteById(id);
    }
}
