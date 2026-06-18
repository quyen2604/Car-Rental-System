package com.carrental.model.entity.Decorator;

public class DashcamDecorator extends BookingDecorator {
    private final double dashcamFee;

    public DashcamDecorator(BookingOrder bookingOrder, double dashcamFee) {
        super(bookingOrder);
        this.dashcamFee = dashcamFee;
    }

    @Override
    public double calculateTotal() {
        return super.bookingOrder.calculateTotal() + dashcamFee;
    }

    @Override
    public String getDescription() {
        return super.bookingOrder.getDescription() + " + Camera hành trình (" + dashcamFee + " VNĐ)";
    }
}
