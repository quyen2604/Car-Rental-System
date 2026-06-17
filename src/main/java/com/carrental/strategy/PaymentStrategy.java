package com.carrental.strategy;

public interface PaymentStrategy {
    // Trả về URL thanh toán (MoMo) hoặc chuỗi trạng thái (Cash)
    String processPay(double amount, String orderId, String orderInfo);

    // Xử lý hoàn tiền (Dùng khi khách hủy đơn)
    void refund(double amount, String orderId);
}