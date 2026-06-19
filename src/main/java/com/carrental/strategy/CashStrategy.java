package com.carrental.strategy;

import com.carrental.model.entity.Decorator.Booking;
import com.carrental.model.entity.Payment;
import com.carrental.model.enums.PaymentStatus;
import com.carrental.model.enums.PaymentType;
import org.springframework.stereotype.Component;

@Component("CASH") // Đặt tên Bean là CASH để xài tự động
public class CashStrategy implements PaymentStrategy {
    @Override
    public String processPay(Payment payment, Booking booking, double amount) {
        // Tiền mặt thì tự cập nhật trạng thái THÀNH CÔNG và tự sinh mã giao dịch
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId("CASH_" + booking.getBookingId());

        return null; // Tiền mặt thì không có link gì để trả về cả
    }

    @Override
    public void refund(double amount, String orderId) {
        System.out.println("💵 [TIỀN MẶT] Hoàn trả tiền mặt cho khách tại quầy: " + amount + " VNĐ cho Đơn " + orderId);
    }
}