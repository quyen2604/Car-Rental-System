package com.carrental.repository;

import com.carrental.model.entity.Decorator.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    // Tìm lịch sử đặt xe của người thuê
    List<Booking> findByRenterUserId(int renterId);

    // Tìm lịch sử đơn đặt xe của chủ xe (Owner)
    List<Booking> findByVehicleOwnerUserId(int ownerId);

    // Tìm các đơn đặt của một xe cụ thể (dùng để check lịch trùng)
    List<Booking> findByVehicleVehicleId(int vehicleId);

    // Kiểm tra lịch đặt xe trùng
    @Query("SELECT b FROM Booking b WHERE b.vehicle.vehicleId = :vehicleId " +
           "AND b.bookingStatus NOT IN (com.carrental.model.enums.BookingStatus.CANCELLED, com.carrental.model.enums.BookingStatus.REJECTED) " +
           "AND (b.startDate <= :endDate AND b.endDate >= :startDate)")
    List<Booking> findOverlappingBookings(
            @Param("vehicleId") int vehicleId,
            @Param("startDate") java.util.Date startDate,
            @Param("endDate") java.util.Date endDate
    );

}
