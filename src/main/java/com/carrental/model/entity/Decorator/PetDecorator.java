package com.carrental.model.entity.Decorator;

public class PetDecorator extends BookingDecorator {
    private final double petFee;

    public PetDecorator(BookingOrder bookingOrder, double petFee) {
        super(bookingOrder);
        this.petFee = petFee;
    }

    @Override
    public double calculateTotal() {
        return super.bookingOrder.calculateTotal() + petFee;
    }

    @Override
    public String getDescription() {
        return super.bookingOrder.getDescription() + " + Gói thú cưng (" + petFee + " VNĐ)";
    }
}
