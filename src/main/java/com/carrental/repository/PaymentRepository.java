package com.carrental.repository;

import com.carrental.model.entity.Payment;
import com.carrental.model.enums.PaymentStatus;
import com.carrental.model.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Tìm tất cả hóa đơn theo mã đặt xe
    List<Payment> findByBookingBookingId(int bookingId);

    // Kiểm tra xem có tồn tại hóa đơn của mã đặt xe này không
    boolean existsByBookingBookingId(int bookingId);

    // Tìm toàn bộ lịch sử thanh toán của một Renter
    List<Payment> findByBookingRenterUserId(int renterId);

    // Tìm hóa đơn theo bookingId và loại thanh toán
    Optional<Payment> findByBookingBookingIdAndPaymentType(int bookingId, PaymentType type);

    // Tìm hóa đơn theo bookingId và trạng thái
    List<Payment> findByBookingBookingIdAndPaymentStatus(int bookingId, PaymentStatus status);
}