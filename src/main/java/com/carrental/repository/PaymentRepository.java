package com.carrental.repository;

import com.carrental.model.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBookingBookingId(int bookingId);

    List<Payment> findByBookingRenterUserId(int renterId);

}
