package com.carrental.model.entity;

import com.carrental.model.enums.PaymentStatus;
import com.carrental.model.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int paymentId;

    private double amount;

    @Temporal(TemporalType.TIMESTAMP)
    private Date paymentDate;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;       // DEPOSIT | FINAL

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;   // PENDING | SUCCESS | FAILED | REFUNDED

    private String paymentMethod;          // MOMO | CASH

    private String transactionId;          // Mã giao dịch từ cổng thanh toán (nếu có)

    private String note;                   // Ghi chú thêm (phí trễ, phí hỏng hóc...)

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;
}
