package com.carrental.repository;

import com.carrental.model.entity.Vehicle;
import com.carrental.model.enums.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {
    // Tìm danh sách xe của một chủ xe cụ thể
    List<Vehicle> findByOwnerUserId(int ownerId);
    // Tìm tất cả xe đang dựa theo thành phố location
    List<Vehicle> findByLocation_City(String city);

    @Query("SELECT c FROM Car c WHERE c.location.city = :city")
    List<Vehicle> findCarsByCity(@Param("city") String city);

    @Query("SELECT m FROM Motorbike m WHERE m.location.city = :city")
    List<Vehicle> findMotorbikesByCity(@Param("city") String city);
}
