package com.carrental.strategy;

import org.springframework.stereotype.Component;

@Component("CASH") // Đặt tên Bean là CASH để xài tự động
public class CashStrategy implements PaymentStrategy {
    @Override
    public String processPay(double amount, String orderId, String orderInfo) {
        System.out.println("💵 [TIỀN MẶT] Khách thanh toán trực tiếp tại quầy: " + amount + " VNĐ cho Đơn " + orderId);
        return "CASH_SUCCESS";
    }

    @Override
    public void refund(double amount, String orderId) {
        System.out.println("💵 [TIỀN MẶT] Hoàn trả tiền mặt cho khách tại quầy: " + amount + " VNĐ cho Đơn " + orderId);
    }
}