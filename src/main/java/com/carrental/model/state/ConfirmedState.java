package com.carrental.model.state;

import com.carrental.model.entity.Booking;
import com.carrental.model.enums.BookingStatus;

public class ConfirmedState implements BookingState {
    @Override
    public void payDeposit(Booking booking) {
        System.out.println("Đóng tiền cọc thành công.");
        booking.setState(new DepositPaidState());
        booking.setBookingStatus(BookingStatus.DEPOSIT_PAID);
    }

    @Override
    public void cancel(Booking booking) {
        System.out.println("Hủy booking thành công.");
        booking.setState(new CancelledState());
        booking.setBookingStatus(BookingStatus.CANCELLED);
    }

    @Override
    public void confirm(Booking booking) {
        throw new IllegalStateException("Booking đã được xác nhận rồi.");
    }

    @Override
    public void pickUpVehicle(Booking booking) {
        throw new IllegalStateException("Cần đóng tiền cọc trước khi nhận xe.");
    }

    @Override
    public void returnVehicle(Booking booking, double lateFee, double damageFee) {
        throw new IllegalStateException("Chưa nhận xe.");
    }

    @Override
    public void complete(Booking booking) {
        throw new IllegalStateException("Không thể hoàn thành đơn ở trạng thái này.");
    }
}
