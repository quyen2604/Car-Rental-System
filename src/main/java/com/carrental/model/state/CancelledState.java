package com.carrental.model.state;

import com.carrental.model.entity.Decorator.Booking;

public class CancelledState implements BookingState {
    @Override
    public void confirm(Booking booking) {
        throw new IllegalStateException("Đơn đã bị hủy.");
    }

    @Override
    public void payDeposit(Booking booking) {
        throw new IllegalStateException("Đơn đã bị hủy.");
    }

    @Override
    public void pickUpVehicle(Booking booking) {
        throw new IllegalStateException("Đơn đã bị hủy.");
    }

    @Override
    public void returnVehicle(Booking booking) {
        throw new IllegalStateException("Đơn đã bị hủy.");
    }
    @Override
    public void complete(Booking booking) {
        throw new IllegalStateException("Đơn đã bị hủy.");
    }
    @Override
    public void cancel(Booking booking) {
        throw new IllegalStateException("Đơn đã bị hủy trước đó.");
    }
}
