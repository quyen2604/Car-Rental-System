package com.carrental.service; // Bắt buộc phải nằm ở dòng đầu tiên

import com.carrental.model.entity.Payment;
import com.carrental.model.enums.PaymentType;
import com.carrental.repository.PaymentRepository;
import com.carrental.strategy.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private PaymentStrategy paymentStrategy = null;

    public void setPaymentStrategy(PaymentStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Chiến lược thanh toán không thể là null.");
        }
        this.paymentStrategy = strategy;
    }

    @Transactional
    public void savePayment(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Thanh toán không hợp lệ!");
        }
        paymentRepository.save(payment);
        System.out.println("Đã lưu thanh toán vào Database.");
    }

    public List<Payment> getPaymentsByBookingId(int bookingId) {
        // Đã sửa lại tên hàm cho khớp với Repository (bỏ dấu gạch dưới)
        if (!paymentRepository.existsByBookingBookingId(bookingId)) {
            throw new IllegalArgumentException("Đặt chỗ không tồn tại!");
        }
        return paymentRepository.findByBookingBookingId(bookingId);
    }

    public void processPay(double amount, PaymentType paymentType) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0.");
        }
        if (paymentStrategy == null) {
            throw new IllegalStateException("Chưa chọn phương thức thanh toán!");
        }
        System.out.println("Đang xử lý thanh toán: " + paymentType + " | Số tiền: " + amount);
        paymentStrategy.processPay(amount);
    }

    // Thực thi: +refund() trên UML
    public void refund(double amount, int bookingId) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số tiền hoàn phải lớn hơn 0.");
        }

        // Kiểm tra xem giao dịch của bookingId này có tồn tại không
        if (!paymentRepository.existsByBookingBookingId(bookingId)) {
            throw new IllegalArgumentException("Không tìm thấy thông tin đặt chỗ cần hoàn tiền!");
        }

        System.out.println("Hệ thống đang hoàn tiền: $" + amount + " cho Booking ID: " + bookingId);
        // Tương lai bạn sẽ gọi API của MOMO/VNPAY tại đây
    }

}