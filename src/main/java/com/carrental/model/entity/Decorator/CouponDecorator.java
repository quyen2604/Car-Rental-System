package com.carrental.model.entity.Decorator;

import com.carrental.model.entity.Coupon;

public class CouponDecorator extends BookingDecorator{
    private Coupon coupon;
    public CouponDecorator(BookingOrder bookingOrder, Coupon coupon) {
        super(bookingOrder);
        this.coupon = coupon;
    }

    @Override
    public double calculateTotal() {
        // Gọi hàm calculateTotal() của interface BookingOrder từ lớp cha để lấy giá gốc
        double originalTotal = super.bookingOrder.calculateTotal();

        // Tính toán giảm giá theo phần trăm từ đối tượng Coupon của bro
        double discountFactor = 1 - (coupon.getPercent() / 100.0);

        return originalTotal * discountFactor;
    }

    @Override
    public String getDescription() {
        // Lấy mô tả gốc và cộng dồn thông tin mã giảm giá
        return super.bookingOrder.getDescription() + " (Áp dụng mã [" + coupon.getCode() + "] giảm " + coupon.getPercent() + "%)";
    }
}
