package com.carrental.strategy;

import org.springframework.stereotype.Component;

@Component("MOMO") // Đặt tên Bean là MOMO để xài tự động
public class MomoStrategy implements PaymentStrategy {
    @Override
    public void processPay(double amount) {
        System.out.println("📱 [MOMO] Đang gọi API MoMo để thu: " + amount + " VNĐ");
    }

    @Override
    public void refund(double amount) {
        System.out.println("📱 [MOMO] Đang gọi API MoMo để hoàn trả: " + amount + " VNĐ");
    }
}