package com.carrental.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private String paymentId; // Mã giao dịch ngân hàng do khách hàng nhập

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "booking_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("paymentList")
    private Booking booking;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false, length = 50)
    private String method; // BANK_TRANSFER, v.v.

    @Column(nullable = false, length = 50)
    private String status; // PENDING, SUCCESS, v.v.

    @CreationTimestamp
    @Column(name = "paid_at", nullable = false, updatable = false)
    private LocalDateTime paidAt;
}
