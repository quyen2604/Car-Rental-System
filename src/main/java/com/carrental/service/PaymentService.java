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

        // Tạo bản ghi thanh toán trạng thái PENDING trước và lưu để lấy paymentId
                Payment payment = createPaymentRecord(booking, req.getAmount(), PaymentType.DEPOSIT,
                        req.getMethod(), req.getNote());
                payment = paymentRepository.save(payment);

        try {
            // 1. Gọi chiến lược xử lý.
            // Thằng CASH sẽ tự set SUCCESS vào payment và trả về null.
            // Thằng MOMO sẽ tự set PENDING vào payment và trả về cái Link.
            String resultUrl = strategy.processPay(payment, booking, req.getAmount());

            // 2. Lưu thông tin hóa đơn đã được Strategy xuống DB
            Payment saved = paymentRepository.save(payment);

            // 3. Đóng gói dữ liệu trả về cho Controller
            PaymentResponse res = mapToResponse(saved);

            // Nếu có link kết quả (như MoMo) thì đính kèm vào cho Frontend dùng
            if (resultUrl != null) {
                res.setPayUrl(resultUrl);
            }

            return res;
        } catch (Exception e) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new RuntimeException("Thanh toán cọc thất bại: " + e.getMessage());
        }

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
        payment = paymentRepository.save(payment);

        try {
            // 1. Thực thi đa hình qua Strategy
            String resultUrl = strategy.processPay(payment, booking, req.getAmount());

            // 2. Đồng bộ lưu hóa đơn sau xử lý xuống DB
            Payment saved = paymentRepository.save(payment);

            // 3. Chuẩn bị dữ liệu DTO trả về cho Frontend
            PaymentResponse res = mapToResponse(saved);

            if (resultUrl != null) {
                res.setPayUrl(resultUrl);
            }

            return res;
        } catch (Exception e) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new RuntimeException("Thanh toán tổng thất bại: " + e.getMessage());
        }

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

        strategy.refund(amount, String.valueOf(bookingId));
        System.out.println("🔄 Đã hoàn tiền " + amount + " VNĐ cho Booking ID: " + bookingId
                + " qua " + method);
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