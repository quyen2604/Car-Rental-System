package com.carrental.repository;

import com.carrental.model.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Tìm hóa đơn theo mã đặt xe
    List<Payment> findByBookingBookingId(int bookingId);

    // Kiểm tra xem có tồn tại hóa đơn của mã đặt xe này không
    boolean existsByBookingBookingId(int bookingId);

    // Tìm toàn bộ lịch sử thanh toán của một User (Renter)
    List<Payment> findByBookingRenterUserId(int renterId);
}