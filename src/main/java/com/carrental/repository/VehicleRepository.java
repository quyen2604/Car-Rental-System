package com.carrental.repository;

import com.carrental.model.entity.Vehicle;
import com.carrental.model.enums.ApprovalStatus;
import com.carrental.model.enums.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {

    @Query("SELECT c FROM Car c WHERE c.location.city = :city AND c.vehicleStatus = 'AVAILABLE' AND c.approvalStatus = 'APPROVED'")
    List<Vehicle> findCarsByCity(@Param("city") String city);

    @Query("SELECT m FROM Motorbike m WHERE m.location.city = :city AND m.vehicleStatus = 'AVAILABLE' AND m.approvalStatus = 'APPROVED'")
    List<Vehicle> findMotorbikesByCity(@Param("city") String city);

    List<Vehicle> findByApprovalStatus(ApprovalStatus approvalStatus);

    List<Vehicle> findByOwnerUserId(int ownerId);
}

