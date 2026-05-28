package com.carrental.repository;

import com.carrental.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByRenterId(Long renterId);
    
    // Tìm các đơn đặt trùng lịch của xe này (không phải trạng thái CANCELLED)
    @Query("SELECT b FROM Booking b WHERE b.vehicle.id = :vehicleId " +
           "AND b.bookingStatus <> 'CANCELLED' " +
           "AND (:startDate <= b.endDate AND :endDate >= b.startDate)")
    List<Booking> findOverlappingBookings(
        @Param("vehicleId") Long vehicleId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
