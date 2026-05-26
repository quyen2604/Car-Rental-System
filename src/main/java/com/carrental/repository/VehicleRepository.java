package com.carrental.repository;

import com.carrental.entity.Vehicle;
import com.carrental.entity.VehicleStatus;
import com.carrental.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    // Tìm theo loại xe
    List<Vehicle> findByVehicleType(VehicleType vehicleType);

    // Tìm theo vị trí (địa chỉ)
    List<Vehicle> findByAddressContainingIgnoreCase(String address);

    // Tìm theo loại xe VÀ vị trí
    List<Vehicle> findByVehicleTypeAndAddressContainingIgnoreCase(VehicleType vehicleType, String address);

    // Tìm theo dòng xe (chứa keyword)
    List<Vehicle> findByModelContainingIgnoreCase(String model);

    // Tìm xe còn trống
    List<Vehicle> findByStatus(VehicleStatus status);

    // Custom query: Tìm linh hoạt theo nhiều điều kiện
    @Query("SELECT v FROM Vehicle v WHERE " +
           "(:vehicleType IS NULL OR v.vehicleType = :vehicleType) AND " +
           "(:address IS NULL OR LOWER(v.address) LIKE LOWER(CONCAT('%', :address, '%'))) AND " +
           "(:status IS NULL OR v.status = :status)")
    List<Vehicle> searchVehicles(
        @Param("vehicleType") VehicleType vehicleType,
        @Param("address") String address,
        @Param("status") VehicleStatus status
    );
}
