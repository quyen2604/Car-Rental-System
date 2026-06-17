package com.carrental.observer;

import com.carrental.DTO.BookingResponse;
import com.carrental.service.BookingService;
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
public class OwnerObserver implements Observer {

    private final BookingService bookingService;
    private static final Map<Integer, String> ownerNotifications = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        if (bookingService instanceof Observable) {
            ((Observable) bookingService).addObserver(this);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        // Giữ luồng gốc phòng trường hợp có luồng khác gọi tới
        if (arg instanceof BookingResponse) {
            BookingResponse booking = (BookingResponse) arg;
            // Luồng Observer gốc bị sai ID nên ta sẽ ưu tiên dùng hàm triggerNotification ở dưới
        }
    }

    public static void triggerNotification(int ownerId) {
        if (ownerId > 0) {
            ownerNotifications.put(ownerId, "Bạn có đơn hàng cần duyệt");
        }
    }

     public static String popNotification(int ownerId) {
        return ownerNotifications.remove(ownerId);
    }
}