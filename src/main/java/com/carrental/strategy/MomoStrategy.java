package com.carrental.strategy;

import com.carrental.model.entity.Decorator.Booking;
import com.carrental.model.entity.Payment;
import com.carrental.model.enums.PaymentStatus;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
@Component("MOMO")
public class MomoStrategy implements PaymentStrategy {

    @Value("${momo.partnerCode}")
    private String partnerCode;

    @Value("${momo.accessKey}")
    private String accessKey;

    @Value("${momo.secretKey}")
    private String secretKey;

    @Value("${momo.endpoint}")
    private String endpoint;

    @Value("${momo.redirectUrl}")
    private String redirectUrl;

    @Value("${momo.ipnUrl}")
    private String ipnUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String processPay(Payment payment, Booking booking, double amount) {
        try {
            // TỰ SINH DỮ LIỆU TỪ INPUT MỚI:
            // 1. Tự sinh orderId cho MoMo bằng cách lấy ID của bản ghi payment vừa lưu + timestamp
            String momoOrderId = payment.getPaymentId() + "_" + System.currentTimeMillis();

            // 2. Tự sinh thông tin đơn hàng từ đối tượng booking
            String orderInfo = "Cọc xe booking " + booking.getBookingId();

            String requestId = String.valueOf(System.currentTimeMillis());
            String requestType = "captureWallet";
            String extraData = "";
            long amountLong = (long) amount;

            // 1. Build the raw signature string (Dùng momoOrderId và orderInfo vừa tạo ở trên)
            String rawHash = "accessKey=" + accessKey +
                    "&amount=" + amountLong +
                    "&extraData=" + extraData +
                    "&ipnUrl=" + ipnUrl +
                    "&orderId=" + momoOrderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + partnerCode +
                    "&redirectUrl=" + redirectUrl +
                    "&requestId=" + requestId +
                    "&requestType=" + requestType;

            // 2. Hash it with HmacSHA256
            String signature = hmacSHA256(rawHash, secretKey);

            // 3. Build the JSON request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", partnerCode);
            requestBody.put("partnerName", "Car Rental");
            requestBody.put("storeId", "MomoStore");
            requestBody.put("requestId", requestId);
            requestBody.put("amount", amountLong);
            requestBody.put("orderId", momoOrderId); // Thay đổi biến ở đây
            requestBody.put("orderInfo", orderInfo); // Thay đổi biến ở đây
            requestBody.put("redirectUrl", redirectUrl);
            requestBody.put("ipnUrl", ipnUrl);
            requestBody.put("lang", "vi");
            requestBody.put("extraData", extraData);
            requestBody.put("requestType", requestType);
            requestBody.put("signature", signature);

            // 4. Send request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(endpoint, entity, Map.class);
            Map<String, Object> resBody = response.getBody();

            if (resBody != null && resBody.containsKey("payUrl")) {
                // TRƯỚC KHI RETURN: Tự động cập nhật trạng thái PENDING cho đối tượng Payment
                payment.setPaymentStatus(PaymentStatus.PENDING);

                return (String) resBody.get("payUrl");
            } else {
                throw new RuntimeException("MoMo response does not contain payUrl: " + resBody);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error processing MoMo payment: " + e.getMessage(), e);
        }
    }

    @Override
    public void refund(double amount, String orderId) {
        System.out.println("📱 [MOMO] Đang gọi API MoMo để hoàn trả: " + amount + " VNĐ cho đơn " + orderId);
        // Implement MoMo refund logic here if needed
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