package com.carrental.notification;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationEvent {
    private String type;
    private String message;
    private int targetUserId;
}
