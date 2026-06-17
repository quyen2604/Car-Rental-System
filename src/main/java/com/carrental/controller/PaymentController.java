package com.carrental.controller;

import com.carrental.DTO.PaymentRequest;
import com.carrental.DTO.PaymentResponse;
import com.carrental.facade.BookingFacade;
import com.carrental.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final BookingFacade bookingFacade;
    private final PaymentService paymentService;

    /**
     * POST /api/payments/deposit
     * Thanh toán cọc tiền (30% tổng tiền) — lưu bản ghi payment vào DB
     * Body: { bookingId, amount, method, note }
     */
    @PostMapping("/deposit")
    public ResponseEntity<?> payDeposit(@RequestBody PaymentRequest req) {
        try {
            PaymentResponse res = bookingFacade.payDepositWithRecord(req);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /api/payments/final
     * Thanh toán tổng (số tiền còn lại sau khi trả xe) — lưu bản ghi + hoàn thành booking
     * Body: { bookingId, amount, method, note }
     */
    @PostMapping("/final")
    public ResponseEntity<?> payFinal(@RequestBody PaymentRequest req) {
        try {
            PaymentResponse res = bookingFacade.payFinalWithRecord(req);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * GET /api/payments/booking/{bookingId}
     * Lấy toàn bộ lịch sử thanh toán của một đơn đặt xe
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<PaymentResponse>> getByBooking(@PathVariable int bookingId) {
        return ResponseEntity.ok(paymentService.getPaymentsByBooking(bookingId));
    }

    /**
     * GET /api/payments/renter/{renterId}
     * Lấy toàn bộ lịch sử thanh toán của một người thuê
     */
    @GetMapping("/renter/{renterId}")
    public ResponseEntity<List<PaymentResponse>> getByRenter(@PathVariable int renterId) {
        return ResponseEntity.ok(paymentService.getPaymentsByRenter(renterId));
    }
}
