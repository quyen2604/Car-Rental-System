package com.carrental.DTO;

import lombok.Data;

@Data
public class PaymentRequest {
    private int bookingId;
    private double amount;
    private String paymentMethod;
    private String method;   // MOMO | CASH
    private String note;     // Ghi chú tuỳ chọn
}
