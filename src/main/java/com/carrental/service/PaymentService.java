package com.carrental.service;

import com.carrental.model.enums.PaymentType;
import com.carrental.repository.PaymentRepository;
import com.carrental.strategy.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    // Kho chứa TẤT CẢ các chiến lược thanh toán (MOMO, CASH...)
    private final Map<String, PaymentStrategy> paymentStrategies;

    @Transactional
    public void processPay(double amount, PaymentType paymentType, String method) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0.");
        }

        // Tự động nhặt chiến lược ra từ Map dựa theo chữ khách chọn (MOMO, CASH)
        PaymentStrategy strategy = paymentStrategies.get(method.toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Phương thức thanh toán không được hỗ trợ: " + method);
        }

        // Thực thi thanh toán
        strategy.processPay(amount);
        System.out.println("✅ Đã xử lý thanh toán loại " + paymentType + " bằng " + method.toUpperCase());
    }

    @Transactional
    public void refund(double amount, int bookingId, String method) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số tiền hoàn phải lớn hơn 0.");
        }

        PaymentStrategy strategy = paymentStrategies.get(method.toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Phương thức thanh toán không được hỗ trợ: " + method);
        }

        // Thực thi hoàn tiền
        strategy.refund(amount);
        System.out.println("🔄 Đã hoàn tiền " + amount + " cho Booking ID: " + bookingId);
    }
}