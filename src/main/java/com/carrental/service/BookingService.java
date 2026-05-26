package com.carrental.service;

import com.carrental.entity.Booking;
import java.time.LocalDate;
import java.util.List;

public interface BookingService {
    Booking createBooking(Long renterId, Long vehicleId, LocalDate startDate, LocalDate endDate, String note,
                          String guestFullName, String guestEmail, String guestPhone,
                          String paymentId, String paymentMethod, Double paymentAmount);
    Booking getBookingById(Long id);
    List<Booking> getBookingsByRenter(Long renterId);
    List<Booking> getAllBookings();
    Booking updateStatus(Long id, String status);
    boolean isVehicleAvailable(Long vehicleId, LocalDate startDate, LocalDate endDate);
}
