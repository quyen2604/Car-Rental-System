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
        // Khóa xe lại không cho người khác đặt
        vehicleService.updateVehicleStatus(booking.getVehicleId(), VehicleStatus.BOOKED);
        return booking;
    }

    // BƯỚC 3: Customer thanh toán cọc (Trạng thái: DEPOSIT_PAID)
    public BookingResponse payDepositForBooking(int bookingId, double amount, String paymentMethod) {
        // Truyền thẳng paymentMethod sang PaymentService để nó tự chọn Strategy
        paymentService.processPay(amount, PaymentType.DEPOSIT, paymentMethod);
        return bookingService.payDeposit(bookingId);
    }

    // BƯỚC 4: Customer nhận xe (Trạng thái: PICKED_UP)
    public BookingResponse pickUpVehicle(int bookingId) {
        return bookingService.pickUpVehicle(bookingId);
    }

    // BƯỚC 5: Customer trả xe (Trạng thái: RETURNED)
    public BookingResponse returnVehicle(int bookingId, double lateFee, double damageFee) {
        return bookingService.returnVehicle(bookingId, lateFee, damageFee);
    }

    // BƯỚC 6: Thanh toán phần còn lại
    public void payRemainingBalance(int bookingId, double amount, String paymentMethod) {
        // Tương tự bước 3, giao hết cho PaymentService xử lý
        paymentService.processPay(amount, PaymentType.DEPOSIT, paymentMethod);
    }

    // BƯỚC 7: Hoàn thành chuyến đi (Trạng thái: COMPLETED)
    public BookingResponse completeBooking(int bookingId) {
        BookingResponse booking = bookingService.completeBooking(bookingId);
        // Trả xe về trạng thái sẵn sàng cho người tiếp theo thuê
        vehicleService.updateVehicleStatus(booking.getVehicleId(), VehicleStatus.AVAILABLE);
        return booking;
    }

    // --- CÁC HÀM TIỆN ÍCH KHÁC ---

    public BookingResponse cancelBooking(int bookingId, String role) {
        BookingResponse cancelledBooking = bookingService.cancelBooking(bookingId, role);
        vehicleService.updateVehicleStatus(cancelledBooking.getVehicleId(), VehicleStatus.AVAILABLE);

        if (cancelledBooking.getRefundAmount() > 0) {
            // Mặc định hoàn qua MOMO (Thực tế nên lưu phương thức lúc thanh toán vào DB để lôi ra dùng)
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