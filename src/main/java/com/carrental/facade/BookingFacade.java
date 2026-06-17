package com.carrental.facade;

import com.carrental.DTO.BookingRequest;
import com.carrental.DTO.BookingResponse;
import com.carrental.service.BookingService;
import com.carrental.service.PaymentService;
import com.carrental.service.VehicleService;
import com.carrental.model.enums.BookingStatus;
import com.carrental.model.enums.VehicleStatus;
import com.carrental.model.enums.PaymentType;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BookingFacade {

    private final VehicleService vehicleService;
    private final PaymentService paymentService;
    private final BookingService bookingService;

    // 1. Tạo đơn đặt xe kèm thanh toán cọc
    public BookingResponse createBookingWithPayment(BookingRequest request, double amount, PaymentType paymentType) {
        if (!vehicleService.checkAvailability(request.getVehicleId())) {
            throw new IllegalStateException("Xe không sẵn sàng để đặt.");
        }

        // Tạo đơn đặt xe
        BookingResponse createdBooking = bookingService.createBooking(request);

        // Xử lý thanh toán tiền cọc
        paymentService.processPay(amount, paymentType);

        // Cập nhật trạng thái Booking thành DEPOSIT_PAID và trả về kết quả
        return bookingService.payDeposit(createdBooking.getBookingId());
    }

    // 2. Hủy đơn đặt xe
    public BookingResponse cancelBooking(int bookingId, String role) {
        BookingResponse cancelledBooking = bookingService.cancelBooking(bookingId, role);

        vehicleService.updateVehicleStatus(cancelledBooking.getVehicleId(), VehicleStatus.AVAILABLE);

        if (cancelledBooking.getRefundAmount() > 0) {
            paymentService.refund(cancelledBooking.getRefundAmount(), bookingId);
        }

        return cancelledBooking;
    }

    // 3. Hoàn thành chuyến đi
    public BookingResponse completeBooking(int bookingId) {
        BookingResponse booking = bookingService.completeBooking(bookingId);
        vehicleService.updateVehicleStatus(booking.getVehicleId(), VehicleStatus.AVAILABLE);
        return booking;
    }

    // 4. Chủ xe xác nhận đơn đặt
    public BookingResponse ownerConfirmBooking(int bookingId) {
        BookingResponse booking = bookingService.approveBooking(bookingId);
        vehicleService.updateVehicleStatus(booking.getVehicleId(), VehicleStatus.BOOKED);
        return booking;
    }

    // 5. Chủ xe từ chối đơn đặt
    public BookingResponse rejectBooking(int bookingId) {
        BookingResponse booking = bookingService.rejectBooking(bookingId);
        // Trả xe về trạng thái sẵn sàng
        vehicleService.updateVehicleStatus(booking.getVehicleId(), VehicleStatus.AVAILABLE);
        // Nếu đã có cọc trước đó (tùy luồng nghiệp vụ), bạn có thể gọi paymentService.refund() ở đây
        return booking;
    }

    // 6. Cập nhật trạng thái thủ công (nếu cần)
    public BookingResponse updateBookingStatus(int bookingId, BookingStatus status) {
        return bookingService.updateBookingStatus(bookingId, status);
    }

    // 7. Lấy thông tin chi tiết Booking
    public BookingResponse getBookingById(int bookingId) {
        return bookingService.getBookingById(bookingId);
    }

    // 8. Lấy danh sách Booking của người thuê
    public List<BookingResponse> getRenterBookings(int renterId) {
        return bookingService.getRenterBookings(renterId);
    }

    // 9. Lấy danh sách Booking của chủ xe
    public List<BookingResponse> getOwnerBookings(int ownerId) {
        return bookingService.getOwnerBookings(ownerId);
    }
}