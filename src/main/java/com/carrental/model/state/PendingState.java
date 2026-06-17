package com.carrental.model.state;

import com.carrental.model.entity.Booking;
import com.carrental.model.enums.BookingStatus;

public class PendingState implements BookingState {
    @Override
    public void confirm(Booking booking) {
        System.out.println("Xác nhận booking thành công.");
        booking.setState(new ConfirmedState());
        booking.setBookingStatus(BookingStatus.CONFIRMED);
    }

    @Override
    public void cancel(Booking booking) {
        System.out.println("Hủy booking thành công.");
        booking.setState(new CancelledState());
        booking.setBookingStatus(BookingStatus.CANCELLED);
    }

    @Override
    public void payDeposit(Booking booking) {
        throw new IllegalStateException("Phải xác nhận booking trước khi đặt cọc.");
    }

    @Override
    public void pickUpVehicle(Booking booking) {
        throw new IllegalStateException("Chưa thể nhận xe ở trạng thái chờ xác nhận.");
    }

    @Override
    public void returnVehicle(Booking booking) {
        throw new IllegalStateException("Chưa nhận xe nên không thể trả xe.");
    }

    @Override
    public void complete(Booking booking) {
        throw new IllegalStateException("Không thể hoàn thành đơn ở trạng thái này.");
    }
}
