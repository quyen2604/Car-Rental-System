package com.carrental.model.entity.Decorator;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class BookingDecorator implements BookingOrder{
    protected  BookingOrder bookingOrder;


}
