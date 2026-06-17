package com.carrental.observer;

import com.carrental.model.entity.Vehicle;
import com.carrental.service.VehicleService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@SuppressWarnings("deprecation")
public class AdminObserver implements Observer {

    private final VehicleService vehicleService;
    // Lưu thông báo dạng cờ (Key: "ADMIN" -> Value: Tin nhắn)
    private static final Map<String, String> adminNotifications = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        if (vehicleService instanceof Observable) {
            ((Observable) vehicleService).addObserver(this);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof Vehicle) {
            // Có bất kỳ xe nào đăng ký mới kích hoạt thông báo cho Admin
            adminNotifications.put("ADMIN", "Bạn có xe mới cần duyệt");
        }
    }

    public static String popNotification() {
        return adminNotifications.remove("ADMIN");
    }
}