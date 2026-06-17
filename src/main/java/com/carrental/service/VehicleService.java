package com.carrental.service;

import com.carrental.DTO.VehicleRequest;
import com.carrental.model.entity.*;
import com.carrental.model.enums.ApprovalStatus;
import com.carrental.model.enums.VehicleStatus;
import com.carrental.repository.UserRepository;
import com.carrental.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    public boolean checkAvailability(int vehicleId) {
        Vehicle v = vehicleRepository.findById(vehicleId).orElse(null);
        return v != null && v.getVehicleStatus() == VehicleStatus.AVAILABLE
                && v.getApprovalStatus() == ApprovalStatus.APPROVED;
    }

    @Transactional
    public void updateVehicleStatus(int vehicleId, VehicleStatus status) {
        Vehicle v = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy xe!"));
        v.setVehicleStatus(status);
        vehicleRepository.save(v);
    }

    @Transactional
    public Vehicle registerVehicle(VehicleRequest request) {
        User user = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.getOwnerId()));

        if (!(user instanceof Owner owner)) {
            throw new IllegalArgumentException("Người dùng với ID " + request.getOwnerId() + " không phải là Owner!");
        }

        // Tạo Location
        Location location = new Location();
        location.setLocationId(UUID.randomUUID().toString().substring(0, 8));
        location.setCity(request.getCity());
        location.setDistrict(request.getDistrict());
        location.setAddressDetail(request.getAddressDetail());

        Vehicle vehicle;
        if ("MOTORBIKE".equalsIgnoreCase(request.getType())) {
            Motorbike motorbike = new Motorbike();
            motorbike.setEngineCapacity(request.getEngineCapacity());
            vehicle = motorbike;
        } else {
            Car car = new Car();
            car.setSeatNumber(request.getSeatNumber());
            vehicle = car;
        }

        vehicle.setBrand(request.getBrand());
        vehicle.setModel(request.getModel());
        vehicle.setLicensePlate(request.getLicensePlate());
        vehicle.setPricePerDay(request.getPricePerDay());
        vehicle.setDescription(request.getDescription());
        vehicle.setOwner(owner);
        vehicle.setLocation(location);
        vehicle.setVehicleStatus(VehicleStatus.AVAILABLE);
        vehicle.setApprovalStatus(ApprovalStatus.PENDING);

        return vehicleRepository.save(vehicle);
    }

    public List<Vehicle> getPendingVehicles() {
        return vehicleRepository.findByApprovalStatus(ApprovalStatus.PENDING);
    }

    public List<Vehicle> getOwnerVehicles(int ownerId) {
        return vehicleRepository.findByOwnerUserId(ownerId);
    }

    @Transactional
    public Vehicle approveVehicle(int vehicleId, String status) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy xe với ID: " + vehicleId));

        ApprovalStatus approvalStatus = ApprovalStatus.valueOf(status.toUpperCase());
        vehicle.setApprovalStatus(approvalStatus);

        // Nếu bị từ chối, đổi trạng thái xe thành MAINTENANCE để không bị đặt
        if (approvalStatus == ApprovalStatus.REJECTED) {
            vehicle.setVehicleStatus(VehicleStatus.MAINTENANCE);
        }

        return vehicleRepository.save(vehicle);
    }
}