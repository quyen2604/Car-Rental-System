package com.carrental.model.entity.Decorator;

public class GPSDecorator extends BookingDecorator {
    private final double gpsFee;

    public GPSDecorator(BookingOrder bookingOrder, double gpsFee) {
        super(bookingOrder);
        this.gpsFee = gpsFee;
    }

    @Override
    public double calculateTotal() {
        return super.bookingOrder.calculateTotal() + gpsFee;
    }

    @Override
    public String getDescription() {
        return super.bookingOrder.getDescription() + " + Định vị GPS (" + gpsFee + " VNĐ)";
    }
}
