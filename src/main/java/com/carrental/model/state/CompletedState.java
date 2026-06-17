package com.carrental.model.state;

import com.carrental.model.entity.Decorator.Booking;

public class CompletedState implements BookingState {
    @Override
    public void confirm(Booking booking) {
        throw new IllegalStateException("Đơn đã hoàn thành.");
    }

    @Override
    public void payDeposit(Booking booking) {
        throw new IllegalStateException("Đơn đã hoàn thành.");
    }

    @Override
    public void pickUpVehicle(Booking booking) {
        throw new IllegalStateException("Đơn đã hoàn thành.");
    }

    @Override
    public void returnVehicle(Booking booking) {
        throw new IllegalStateException("Đơn đã hoàn thành.");
    }

    @Override
    public void complete(Booking booking) {
        throw new IllegalStateException("Đơn đã hoàn thành trước đó.");
    }

    @Override
    public void cancel(Booking booking) {
        throw new IllegalStateException("Đơn đã hoàn thành, không thể hủy.");
    }
}
