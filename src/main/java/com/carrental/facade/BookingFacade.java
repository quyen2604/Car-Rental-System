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
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Điều phối luồng nghiệp vụ tổng thể. Kết nối các phân hệ độc lập lại với nhau.
 */
@Transactional
@Component
@RequiredArgsConstructor
public class BookingFacade {

    private final VehicleService vehicleService;
    private final PaymentService paymentService;
    private final BookingService bookingService;

    // BƯỚC 1: Khách hàng tạo yêu cầu đặt xe
    public BookingResponse createBookingOnly(BookingRequest request) {
        if (!vehicleService.checkAvailability(request.getVehicleId())) {
            throw new IllegalStateException("Xe không sẵn sàng để đặt.");
        }
        return bookingService.createBooking(request);
    }

    // BƯỚC 2: Chủ xe xác nhận cho thuê
    public BookingResponse ownerConfirmBooking(int bookingId) {
        return bookingService.approveBooking(bookingId);
    }

    // BƯỚC 3: Khách hàng thanh toán tiền cọc (Luồng qua DTO)
    public PaymentResponse payDepositWithRecord(PaymentRequest req) {
        // Ghi nhận giao dịch thanh toán
        PaymentResponse paymentRes = paymentService.processDeposit(req);
        // Cập nhật trạng thái Booking
        bookingService.payDeposit(req.getBookingId());
        return paymentRes;
    }

    // BƯỚC 3.1: Khách hàng thanh toán tiền cọc (Luồng qua Parameter trực tiếp)
    public BookingResponse payDepositForBooking(int bookingId, double amount, String paymentMethod) {
        PaymentRequest req = new PaymentRequest();
        req.setBookingId(bookingId);
        req.setAmount(amount);
        req.setPaymentMethod(paymentMethod);

        paymentService.processDeposit(req);
        return bookingService.payDeposit(bookingId);
    }

    // BƯỚC 4: Khách hàng đến nhận xe
    public BookingResponse pickUpVehicle(int bookingId) {
        BookingResponse booking = bookingService.pickUpVehicle(bookingId);
        // Chuyển trạng thái xe để hệ thống biết xe đang vận hành thực tế
        vehicleService.updateVehicleStatus(booking.getVehicleId(), VehicleStatus.RENTING);
        return booking;
    }

    // BƯỚC 5: Khách hàng trả xe
    public BookingResponse returnVehicle(int bookingId, double lateFee, double damageFee) {
        // Ghi nhận thời điểm trả và cập nhật phụ phí phát sinh
        return bookingService.returnVehicle(bookingId, lateFee, damageFee);
    }

    // BƯỚC 6: Khách hàng thanh toán phần tiền còn lại và hoàn tất chuyến đi (Luồng qua DTO)
    public PaymentResponse payFinalWithRecord(PaymentRequest req) {
        // Xử lý thanh toán hóa đơn cuối cùng
        PaymentResponse paymentRes = paymentService.processFinalPayment(req);

        // Đóng hồ sơ chuyến đi
        BookingResponse booking = bookingService.completeBooking(req.getBookingId());

        // Trả xe về trạng thái sẵn sàng đón khách mới
        vehicleService.updateVehicleStatus(booking.getVehicleId(), VehicleStatus.AVAILABLE);
        return paymentRes;
    }

    // BƯỚC 6.1: Khách hàng thanh toán phần tiền còn lại (Luồng qua Parameter trực tiếp)
    public void payRemainingBalance(int bookingId, double amount, String paymentMethod) {
        PaymentRequest req = new PaymentRequest();
        req.setBookingId(bookingId);
        req.setAmount(amount);
        req.setPaymentMethod(paymentMethod);

        paymentService.processFinalPayment(req);
    }

    // BƯỚC 7: Hoàn tất đơn hàng (Xử lý độc lập việc kết thúc hành trình và giải phóng xe)
    public BookingResponse completeBooking(int bookingId) {
        BookingResponse booking = bookingService.completeBooking(bookingId);
        vehicleService.updateVehicleStatus(booking.getVehicleId(), VehicleStatus.AVAILABLE);
        return booking;
    }

    // --- Các luồng xử lý ngoại lệ (Hủy/Từ chối) ---

    public BookingResponse cancelBooking(int bookingId, String role) {
        BookingResponse cancelledBooking = bookingService.cancelBooking(bookingId, role);

        // Kích hoạt tiến trình hoàn tiền tự động nếu hệ thống ghi nhận có khoản cần hoàn trả
        if (cancelledBooking.getRefundAmount() > 0) {
            paymentService.refund(cancelledBooking.getRefundAmount(), bookingId, "MOMO");
        }
        return cancelledBooking;
    }

    public BookingResponse rejectBooking(int bookingId) {
        return bookingService.rejectBooking(bookingId);
    }

    // --- Các hàm truy xuất dữ liệu ---

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