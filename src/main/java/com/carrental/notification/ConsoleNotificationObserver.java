package com.carrental.notification;

import org.springframework.stereotype.Component;

@Component
public class ConsoleNotificationObserver implements NotificationObserver {

    public ConsoleNotificationObserver(NotificationSubject notificationSubject) {
        notificationSubject.registerObserver(this);
    }

    @Override
    public void update(NotificationEvent event) {
        System.out.println("\n🔔 [HỆ THỐNG THÔNG BÁO TỰ ĐỘNG] 🔔");
        System.out.println("-> Thể loại: " + event.getType());
        System.out.println("-> Gửi đến User ID: " + event.getTargetUserId());
        System.out.println("-> Nội dung tin nhắn: " + event.getMessage());
        System.out.println("=========================================\n");
    }
}