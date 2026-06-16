package com.carrental.service;

import com.carrental.model.entity.Booking;
import com.carrental.model.entity.Payment;
import com.carrental.model.enums.BookingStatus;
import com.carrental.model.enums.PaymentType;
import com.carrental.notification.NotificationEvent;
import com.carrental.notification.NotificationSubject;
import com.carrental.repository.BookingRepository;
import com.carrental.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final NotificationSubject notificationSubject;

    @Transactional
    public Payment processPayment(int bookingId, double amount, PaymentType type) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + bookingId));

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setPaymentDate(new Date());
        payment.setPaymentType(type);
        Payment savedPayment = paymentRepository.save(payment);

        // Sau khi thanh toán, cập nhật trạng thái đơn đặt xe tùy loại giao dịch
        booking.setBookingStatus(type == PaymentType.DEPOSIT ? BookingStatus.DEPOSIT_PAID : BookingStatus.COMPLETED);
        bookingRepository.save(booking);

        //  Phát thông báo đến Chủ xe khi Renter nạp tiền thành công qua Observer Pattern
        if (booking.getVehicle() != null && booking.getVehicle().getOwner() != null) {
            int ownerId = booking.getVehicle().getOwner().getUserId();
            String msg = String.format("Đơn hàng #%d cho xe %s %s đã được khách hàng thanh toán thành công số tiền %sđ (%s).",
                    booking.getBookingId(), booking.getVehicle().getBrand(), booking.getVehicle().getModel(), String.format("%,.0f", amount), type.name());

            notificationSubject.notifyObservers(new NotificationEvent("PAYMENT_SUCCESS", msg, ownerId));
        }

        return savedPayment;
    }
}