package com.carrental.model.entity.Decorator;

public class BabySeatDecorator extends BookingDecorator {
    private final double babySeatFee;

    public BabySeatDecorator(BookingOrder bookingOrder, double babySeatFee) {
        super(bookingOrder);
        this.babySeatFee = babySeatFee;
    }

    @Override
    public double calculateTotal() {
        return super.bookingOrder.calculateTotal() + babySeatFee;
    }

    @Override
    public String getDescription() {
        return super.bookingOrder.getDescription() + " + Ghế trẻ em (" + babySeatFee + " VNĐ)";
    }
}
