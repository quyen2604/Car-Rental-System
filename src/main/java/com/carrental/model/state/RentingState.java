package com.carrental.model.state;

import com.carrental.model.entity.Booking;
import com.carrental.model.enums.BookingStatus;

public class RentingState implements BookingState {
    @Override
    public void returnVehicle(Booking booking) {
        System.out.println("Trả xe thành công. Đã chuyển sang trạng thái RETURNED.");
        booking.setState(new ReturnedState());
        booking.setBookingStatus(BookingStatus.RETURNED);
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
        throw new IllegalStateException("Khách đang giữ xe rồi.");
    }

    @Override
    public void complete(Booking booking) {
        throw new IllegalStateException("Cần trả xe trước khi hoàn thành.");
    }

    @Override
    public void cancel(Booking booking) {
        throw new IllegalStateException("Khách đang cầm xe, không thể hủy đơn ngay. Yêu cầu trả xe trước.");
    }
}
