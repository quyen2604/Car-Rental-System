package com.carrental.DTO;

import lombok.Data;
import java.util.Date;

@Data
public class PaymentResponse {
    private int paymentId;
    private int bookingId;
    private double amount;
    private String paymentType;    // DEPOSIT | FINAL
    private String paymentStatus;  // PENDING | SUCCESS | FAILED | REFUNDED
    private String paymentMethod;  // MOMO | CASH
    private String transactionId;
    private String note;
    private Date paymentDate;
    private String payUrl; // Redirect URL for e-wallets
    // Thông tin booking kèm theo cho tiện hiển thị
    private String vehicleBrand;
    private String vehicleModel;
    private String renterName;
}
