package com.carrental.controller;

import com.carrental.model.entity.Payment;
import com.carrental.model.enums.PaymentStatus;
import com.carrental.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/payment/momo-ipn")
@RequiredArgsConstructor
public class MomoIPNController {

    private final PaymentRepository paymentRepository;

    @Value("${momo.accessKey}")
    private String accessKey;

    @Value("${momo.secretKey}")
    private String secretKey;

    @PostMapping
    public ResponseEntity<Void> handleIPN(@RequestBody Map<String, Object> payload) {
        try {
            // Read fields
            String partnerCode = (String) payload.get("partnerCode");
            String orderId = (String) payload.get("orderId");
            String requestId = (String) payload.get("requestId");
            Number amountRaw = (Number) payload.get("amount");
            long amount = amountRaw != null ? amountRaw.longValue() : 0;
            String orderInfo = (String) payload.get("orderInfo");
            String orderType = (String) payload.get("orderType");
            String transId = String.valueOf(payload.get("transId"));
            Number resultCodeRaw = (Number) payload.get("resultCode");
            int resultCode = resultCodeRaw != null ? resultCodeRaw.intValue() : -1;
            String message = (String) payload.get("message");
            String payType = (String) payload.get("payType");
            String responseTime = String.valueOf(payload.get("responseTime"));
            String extraData = (String) payload.get("extraData");
            String signature = (String) payload.get("signature");

            // Build raw hash string to verify signature
            String rawHash = "accessKey=" + accessKey +
                    "&amount=" + amount +
                    "&extraData=" + extraData +
                    "&message=" + message +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&orderType=" + orderType +
                    "&partnerCode=" + partnerCode +
                    "&payType=" + payType +
                    "&requestId=" + requestId +
                    "&responseTime=" + responseTime +
                    "&resultCode=" + resultCode +
                    "&transId=" + transId;

            String expectedSignature = hmacSHA256(rawHash, secretKey);

            if (!expectedSignature.equals(signature)) {
                System.out.println("❌ [MOMO IPN] Invalid signature for Order " + orderId);
                return ResponseEntity.badRequest().build();
            }

            // Logic to update DB
            if (orderId != null) {
                Integer paymentId = Integer.parseInt(orderId);
                Payment payment = paymentRepository.findById(Long.valueOf(paymentId)).orElse(null);
                if (payment != null && payment.getPaymentStatus() == PaymentStatus.PENDING) {
                    if (resultCode == 0) {
                        payment.setPaymentStatus(PaymentStatus.SUCCESS);
                        payment.setTransactionId(transId);
                        paymentRepository.save(payment);
                        System.out.println("✅ [MOMO IPN] Thanh toán THÀNH CÔNG cho Đơn " + orderId);
                    } else {
                        payment.setPaymentStatus(PaymentStatus.FAILED);
                        payment.setTransactionId(transId);
                        paymentRepository.save(payment);
                        System.out.println("❌ [MOMO IPN] Thanh toán THẤT BẠI cho Đơn " + orderId + " - Lỗi: " + message);
                    }
                }
            }
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    private String hmacSHA256(String data, String key) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
