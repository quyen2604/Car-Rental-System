package com.carrental.service;

import com.carrental.DTO.PaymentRequest;
import com.carrental.DTO.PaymentResponse;
import com.carrental.model.entity.Decorator.Booking;
import com.carrental.model.entity.Payment;
import com.carrental.model.enums.PaymentStatus;
import com.carrental.model.enums.PaymentType;
import com.carrental.repository.BookingRepository;
import com.carrental.repository.PaymentRepository;
import com.carrental.strategy.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    // Kho chứa TẤT CẢ các chiến lược thanh toán (MOMO, CASH...)
    private final Map<String, PaymentStrategy> paymentStrategies;

    // =========================================================
    // THANH TOÁN CỌC (30% tổng tiền)
    // =========================================================
    @Transactional
    public PaymentResponse processDeposit(PaymentRequest req) {
        if (req.getAmount() <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0.");
        }

        Booking booking = bookingRepository.findById(req.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy đơn đặt xe với ID: " + req.getBookingId()));

        // Chọn chiến lược thanh toán (MOMO / CASH)
        PaymentStrategy strategy = resolveStrategy(req.getMethod());

        // Tạo bản ghi thanh toán trạng thái PENDING trước
        Payment payment = createPaymentRecord(booking, req.getAmount(),
                PaymentType.DEPOSIT, req.getMethod(), req.getNote());

        try {
            // Thực thi thanh toán qua Strategy
            strategy.processPay(req.getAmount());

            // Cập nhật trạng thái SUCCESS
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(generateTransactionId("DEP", booking.getBookingId()));
            System.out.println("✅ [DEPOSIT] Cọc tiền thành công - Booking #" + booking.getBookingId()
                    + " | Số tiền: " + req.getAmount() + " VNĐ | Phương thức: " + req.getMethod());
        } catch (Exception e) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new RuntimeException("Thanh toán cọc thất bại: " + e.getMessage());
        }

        Payment saved = paymentRepository.save(payment);
        return mapToResponse(saved);
    }

    // =========================================================
    // THANH TOÁN TỔNG (phần còn lại sau khi trả xe)
    // =========================================================
    @Transactional
    public PaymentResponse processFinalPayment(PaymentRequest req) {
        if (req.getAmount() <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0.");
        }

        Booking booking = bookingRepository.findById(req.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy đơn đặt xe với ID: " + req.getBookingId()));

        PaymentStrategy strategy = resolveStrategy(req.getMethod());

        Payment payment = createPaymentRecord(booking, req.getAmount(),
                PaymentType.FINAL, req.getMethod(), req.getNote());

        try {
            strategy.processPay(req.getAmount());

            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(generateTransactionId("FIN", booking.getBookingId()));
            System.out.println("✅ [FINAL] Thanh toán tổng thành công - Booking #" + booking.getBookingId()
                    + " | Số tiền: " + req.getAmount() + " VNĐ | Phương thức: " + req.getMethod());
        } catch (Exception e) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new RuntimeException("Thanh toán tổng thất bại: " + e.getMessage());
        }

        Payment saved = paymentRepository.save(payment);
        return mapToResponse(saved);
    }

    // =========================================================
    // HOÀN TIỀN
    // =========================================================
    @Transactional
    public void refund(double amount, int bookingId, String method) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số tiền hoàn phải lớn hơn 0.");
        }

        PaymentStrategy strategy = resolveStrategy(method);
        strategy.refund(amount);
        System.out.println("🔄 Đã hoàn tiền " + amount + " VNĐ cho Booking ID: " + bookingId
                + " qua " + method);
    }

    // =========================================================
    // LEGACY METHOD (giữ nguyên để không breaking change)
    // =========================================================
    @Transactional
    public void processPay(double amount, PaymentType paymentType, String method) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0.");
        }
        PaymentStrategy strategy = resolveStrategy(method);
        strategy.processPay(amount);
        System.out.println("✅ Đã xử lý thanh toán loại " + paymentType + " bằng " + method.toUpperCase());
    }

    // =========================================================
    // TRUY VẤN LỊCH SỬ THANH TOÁN
    // =========================================================
    public List<PaymentResponse> getPaymentsByBooking(int bookingId) {
        return paymentRepository.findByBookingBookingId(bookingId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<PaymentResponse> getPaymentsByRenter(int renterId) {
        return paymentRepository.findByBookingRenterUserId(renterId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // =========================================================
    // HELPERS
    // =========================================================
    private PaymentStrategy resolveStrategy(String method) {
        PaymentStrategy strategy = paymentStrategies.get(method.toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Phương thức thanh toán không được hỗ trợ: " + method);
        }
        return strategy;
    }

    private Payment createPaymentRecord(Booking booking, double amount,
                                        PaymentType type, String method, String note) {
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setPaymentType(type);
        payment.setPaymentMethod(method.toUpperCase());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentDate(new Date());
        payment.setNote(note);
        return payment;
    }

    private String generateTransactionId(String prefix, int bookingId) {
        return prefix + "-" + bookingId + "-" + System.currentTimeMillis();
    }

    private PaymentResponse mapToResponse(Payment p) {
        PaymentResponse res = new PaymentResponse();
        res.setPaymentId(p.getPaymentId());
        res.setAmount(p.getAmount());
        res.setPaymentType(p.getPaymentType() != null ? p.getPaymentType().name() : null);
        res.setPaymentStatus(p.getPaymentStatus() != null ? p.getPaymentStatus().name() : null);
        res.setPaymentMethod(p.getPaymentMethod());
        res.setTransactionId(p.getTransactionId());
        res.setNote(p.getNote());
        res.setPaymentDate(p.getPaymentDate());

        if (p.getBooking() != null) {
            res.setBookingId(p.getBooking().getBookingId());
            if (p.getBooking().getVehicle() != null) {
                res.setVehicleBrand(p.getBooking().getVehicle().getBrand());
                res.setVehicleModel(p.getBooking().getVehicle().getModel());
            }
            if (p.getBooking().getRenter() != null) {
                res.setRenterName(p.getBooking().getRenter().getFullName());
            }
        }
        return res;
    }
}