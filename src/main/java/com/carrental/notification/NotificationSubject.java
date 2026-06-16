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

    // Đăng ký một Observer mới vào hệ thống
    public void registerObserver(NotificationObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    // Hủy đăng ký Observer
    public void removeObserver(NotificationObserver observer) {
        observers.remove(observer);
    }

    // Phát thông báo đến toàn bộ các Observer đang lắng nghe
    public void notifyObservers(NotificationEvent event) {
        for (NotificationObserver observer : observers) {
            observer.update(event);
        }
    }
    // TRONG CLASS XỬ LÝ NOTIFY OBSERVERS CỦA BẠN (Subject hoặc ObserverImpl)
    @Autowired
    private NotificationRepository notificationRepository;

    public void sendNotificationToUser(int userId, String title, String message, String type) {
        // 1. In log ra console (phần bạn đã làm thành công)
        System.out.println("🔔 [HỆ THỐNG THÔNG BÁO TỰ ĐỘNG] 🔔");
        System.out.println("-> Gửi đến User ID: " + userId);
        System.out.println("-> Nội dung: " + message);

        // 2. LƯU THỰC TẾ XUỐNG DATABASE MYSQL
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false); // Mặc định là chưa đọc

        notificationRepository.save(notification); // Lưu thành công xuống bảng notifications!
    }
}