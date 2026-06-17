package com.carrental.model.state;

import com.carrental.model.entity.Decorator.Booking;
import com.carrental.model.enums.BookingStatus;

public class DepositPaidState implements BookingState {
    @Override
    public void pickUpVehicle(Booking booking) {
        System.out.println("Nhận xe thành công. Đang trong quá trình thuê.");
        booking.setState(new RentingState());
        booking.setBookingStatus(BookingStatus.RENTING);
    }

    @Override
    public void cancel(Booking booking) {
        System.out.println("Hủy booking thành công (Có thể sẽ xử lý hoàn cọc).");
        // GỌI LOGIC REFUND Ở ĐÂY
        // paymentService.processRefund(booking, refundRatio);
        booking.setState(new CancelledState());
        booking.setBookingStatus(BookingStatus.CANCELLED);
    }

    @Override
    public void confirm(Booking booking) {
        throw new IllegalStateException("Booking đã được xác nhận trước đó.");
    }

    @Override
    public void payDeposit(Booking booking) {
        throw new IllegalStateException("Đã đóng tiền cọc rồi.");
    }

    @Override
    public void returnVehicle(Booking booking) {
        throw new IllegalStateException("Chưa nhận xe.");
    }

    @Override
    public void complete(Booking booking) {
        throw new IllegalStateException("Không thể hoàn thành đơn ở trạng thái này.");
    }
}
