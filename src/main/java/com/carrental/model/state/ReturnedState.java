package com.carrental.model.state;

import com.carrental.model.entity.Booking;
import com.carrental.model.enums.BookingStatus;

public class ReturnedState implements BookingState {
    @Override
    public void complete(Booking booking) {
        System.out.println("Hoàn tất thanh toán và kết thúc đơn thuê xe.");
        booking.setState(new CompletedState());
        booking.setBookingStatus(BookingStatus.COMPLETED);
    }

    @Override
    public void confirm(Booking booking) {
        throw new IllegalStateException("Thao tác không hợp lệ.");
    }

    @Override
    public void payDeposit(Booking booking) {
        throw new IllegalStateException("Thao tác không hợp lệ.");
    }

    @Override
    public void pickUpVehicle(Booking booking) {
        throw new IllegalStateException("Thao tác không hợp lệ.");
    }

    @Override
    public void returnVehicle(Booking booking) {
        throw new IllegalStateException("Đã trả xe rồi.");
    }

    @Override
    public void cancel(Booking booking) {
        throw new IllegalStateException("Xe đã trả, không thể hủy đơn.");
    }
}
