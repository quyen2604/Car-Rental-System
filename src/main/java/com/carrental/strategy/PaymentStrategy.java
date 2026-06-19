package com.carrental.strategy;

import com.carrental.model.entity.Decorator.Booking;
import com.carrental.model.entity.Payment;

public interface PaymentStrategy {
    // Trả về URL thanh toán (MoMo) hoặc chuỗi trạng thái (Cash)
    String processPay(Payment payment, Booking booking, double amount);

    // Xử lý hoàn tiền (Dùng khi khách hủy đơn)
    void refund(double amount, String orderId);
}