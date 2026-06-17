package com.carrental.model.state;

import com.carrental.model.entity.Booking;

public interface BookingState {
    void confirm(Booking booking);
    void payDeposit(Booking booking);
    void pickUpVehicle(Booking booking);
    void returnVehicle(Booking booking);
    void complete(Booking booking);
    void cancel(Booking booking);
}
