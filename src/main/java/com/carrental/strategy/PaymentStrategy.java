package com.carrental.strategy;

public interface PaymentStrategy {
    // Xử lý thu tiền
    void processPay(double amount);

    // Xử lý hoàn tiền (Dùng khi khách hủy đơn)
    void refund(double amount);
}