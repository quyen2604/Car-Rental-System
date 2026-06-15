package com.carrental.repository;

import com.carrental.model.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    // Tìm lịch sử đặt xe của người thuê
    List<Booking> findByRenterUserId(int renterId);

    // Tìm các đơn đặt của một xe cụ thể (dùng để check lịch trùng)
    List<Booking> findByVehicleVehicleId(int vehicleId);
}
