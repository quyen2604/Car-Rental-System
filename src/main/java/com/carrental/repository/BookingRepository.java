package com.carrental.repository;

import com.carrental.model.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    // TỰ ĐỊNH NGHĨA HÀM TÌM KIẾM THEO ĐÚNG BIẾN bookingId CỦA BẠN
    Optional<Booking> findByBookingId(int bookingId);

    // Lịch sử đặt xe cũ của bạn giữ nguyên...
    List<Booking> findByRenterUserId(int renterId);
    List<Booking> findByVehicleVehicleId(int vehicleId);

    @Query("SELECT b FROM Booking b WHERE b.vehicle.vehicleId = :vehicleId " +
            "AND b.bookingStatus NOT IN (com.carrental.model.enums.BookingStatus.CANCELLED, com.carrental.model.enums.BookingStatus.REJECTED) " +
            "AND (b.startDate <= :endDate AND b.endDate >= :startDate)")
    List<Booking> findOverlappingBookings(
            @Param("vehicleId") int vehicleId,
            @Param("startDate") java.util.Date startDate,
            @Param("endDate") java.util.Date endDate
    );
    List<com.carrental.model.entity.Booking> findByVehicle_Owner_UserId(int ownerId);
}