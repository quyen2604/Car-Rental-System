package com.carrental.controller;

import com.carrental.model.entity.Payment;
import com.carrental.model.enums.PaymentType;
import com.carrental.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestParam int bookingId, @RequestParam double amount, @RequestParam PaymentType type) {
        try {
            Payment payment = paymentService.processPayment(bookingId, amount, type);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}