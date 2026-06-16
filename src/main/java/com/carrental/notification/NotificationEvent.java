package com.carrental.notification;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationEvent {
    private String type;      // Loại sự kiện: VEHICLE_REGISTER, BOOKING_CREATED, PAYMENT_SUCCESS
    private String message;   // Nội dung thông báo chi tiết
    private int targetUserId; // ID của người nhận (Admin hoặc Owner)
}
