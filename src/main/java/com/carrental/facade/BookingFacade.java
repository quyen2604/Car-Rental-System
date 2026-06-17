package com.carrental.facade;

import com.carrental.DTO.BookingRequest;
import com.carrental.DTO.BookingResponse;
import com.carrental.DTO.PaymentRequest;
import com.carrental.DTO.PaymentResponse;
import com.carrental.service.BookingService;
import com.carrental.service.PaymentService;
import com.carrental.service.VehicleService;
import com.carrental.model.enums.BookingStatus;
import com.carrental.model.enums.VehicleStatus;
import com.carrental.model.enums.PaymentType;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Component
@RequiredArgsConstructor
public class BookingFacade {

    private final VehicleService vehicleService;
    private final PaymentService paymentService;
    private final BookingService bookingService;

    // BƯỚC 1: Customer tạo Booking (Trạng thái: PENDING)
    public BookingResponse createBookingOnly(BookingRequest request) {
        if (!vehicleService.checkAvailability(request.getVehicleId())) {
            throw new IllegalStateException("Xe không sẵn sàng để đặt.");
        }
        return bookingService.createBooking(request);
    }

    // BƯỚC 2: Owner xác nhận đơn (Trạng thái: CONFIRMED)
    public BookingResponse ownerConfirmBooking(int bookingId) {
        BookingResponse booking = bookingService.approveBooking(bookingId);
        vehicleService.updateVehicleStatus(booking.getVehicleId(), VehicleStatus.BOOKED);
        return booking;
    }

    // BƯỚC 3: Customer thanh toán cọc (Trạng thái: DEPOSIT_PAID)
    // Gọi từ BookingController (legacy endpoint dùng query param)
    public BookingResponse payDepositForBooking(int bookingId, double amount, String paymentMethod) {
        paymentService.processPay(amount, PaymentType.DEPOSIT, paymentMethod);
        return bookingService.payDeposit(bookingId);
    }

    // BƯỚC 3 (mới): Thanh toán cọc qua PaymentController (lưu DB đầy đủ)
    public PaymentResponse payDepositWithRecord(PaymentRequest req) {
        PaymentResponse paymentRes = paymentService.processDeposit(req);
        bookingService.payDeposit(req.getBookingId());
        return paymentRes;
    }

    // BƯỚC 4: Customer nhận xe (Trạng thái: RENTING)
    public BookingResponse pickUpVehicle(int bookingId) {
        return bookingService.pickUpVehicle(bookingId);
    }

    // BƯỚC 5: Customer trả xe (Trạng thái: RETURNED)
    public BookingResponse returnVehicle(int bookingId, double lateFee, double damageFee) {
        return bookingService.returnVehicle(bookingId, lateFee, damageFee);
    }

    // BƯỚC 6: Thanh toán phần còn lại (BUG FIX: dùng PaymentType.FINAL, không phải DEPOSIT)
    public void payRemainingBalance(int bookingId, double amount, String paymentMethod) {
        paymentService.processPay(amount, PaymentType.FINAL, paymentMethod);
    }

    // BƯỚC 6 (mới): Thanh toán tổng qua PaymentController (lưu DB đầy đủ)
    public PaymentResponse payFinalWithRecord(PaymentRequest req) {
        PaymentResponse paymentRes = paymentService.processFinalPayment(req);
        bookingService.completeBooking(req.getBookingId());
        vehicleService.updateVehicleStatus(
            bookingService.getBookingById(req.getBookingId()).getVehicleId(),
            VehicleStatus.AVAILABLE);
        return paymentRes;
    }

    // BƯỚC 7: Hoàn thành chuyến đi (Trạng thái: COMPLETED)
    public BookingResponse completeBooking(int bookingId) {
        BookingResponse booking = bookingService.completeBooking(bookingId);
        vehicleService.updateVehicleStatus(booking.getVehicleId(), VehicleStatus.AVAILABLE);
        return booking;
    }

    // --- CÁC HÀM TIỆN ÍCH KHÁC ---

    public BookingResponse cancelBooking(int bookingId, String role) {
        BookingResponse cancelledBooking = bookingService.cancelBooking(bookingId, role);
        vehicleService.updateVehicleStatus(cancelledBooking.getVehicleId(), VehicleStatus.AVAILABLE);
        if (cancelledBooking.getRefundAmount() > 0) {
            paymentService.refund(cancelledBooking.getRefundAmount(), bookingId, "MOMO");
        }
        return cancelledBooking;
    }

    public BookingResponse rejectBooking(int bookingId) {
        BookingResponse booking = bookingService.rejectBooking(bookingId);
        vehicleService.updateVehicleStatus(booking.getVehicleId(), VehicleStatus.AVAILABLE);
        return booking;
    }

    public BookingResponse updateBookingStatus(int bookingId, BookingStatus status) {
        return bookingService.updateBookingStatus(bookingId, status);
    }

    public BookingResponse getBookingById(int bookingId) {
        return bookingService.getBookingById(bookingId);
    }

    public List<BookingResponse> getRenterBookings(int renterId) {
        return bookingService.getRenterBookings(renterId);
    }

    public List<BookingResponse> getOwnerBookings(int ownerId) {
        return bookingService.getOwnerBookings(ownerId);
    }
}