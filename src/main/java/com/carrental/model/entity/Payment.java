package com.carrental.model.entity;
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
    private PaymentType paymentType;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;
}
