package com.carrental.notification;

import com.carrental.model.entity.Notification;
import com.carrental.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class NotificationSubject {
    private final List<NotificationObserver> observers = new ArrayList<>();

    public void registerObserver(NotificationObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    // Hủy đăng ký Observer
    public void removeObserver(NotificationObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(NotificationEvent event) {
        for (NotificationObserver observer : observers) {
            observer.update(event);
        }
    }
    @Autowired
    private NotificationRepository notificationRepository;

    public void sendNotificationToUser(int userId, String title, String message, String type) {
        System.out.println("🔔 [HỆ THỐNG THÔNG BÁO TỰ ĐỘNG] 🔔");
        System.out.println("-> Gửi đến User ID: " + userId);
        System.out.println("-> Nội dung: " + message);

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);

        notificationRepository.save(notification);
    }
}