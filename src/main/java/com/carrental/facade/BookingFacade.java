package com.carrental.facade;

import com.carrental.DTO.BookingRequest;
import com.carrental.DTO.BookingResponse;
import com.carrental.service.BookingService;
import com.carrental.service.PaymentService;
import com.carrental.service.VehicleService;
import com.carrental.model.enums.VehicleStatus;
import com.carrental.model.enums.PaymentType;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor // Lombok tự động tạo Constructor cho các biến 'final'
public class BookingFacade {

    private final VehicleService vehicleService;
    private final PaymentService paymentService;
    private final BookingService bookingService;

    // 1. Tạo đơn đặt xe kèm thanh toán cọc
    public void createBookingWithPayment(BookingRequest request, double amount, PaymentType paymentType) {
        // Kiểm tra xem xe có sẵn sàng không
        if (!vehicleService.checkAvailability(request.getVehicleId())) {
            throw new IllegalStateException("Xe không sẵn sàng để đặt.");
        }

        // Tạo đơn đặt xe (Trạng thái ban đầu: PENDING)
        BookingResponse createdBooking = bookingService.createBooking(request);

        // Xử lý thanh toán tiền cọc
        paymentService.processPay(amount, paymentType);

        // Cập nhật trạng thái Booking thành DEPOSIT_PAID
        bookingService.payDeposit(createdBooking.getBookingId());
    }

    // 2. Hủy đơn đặt xe (Áp dụng cho cả Renter và Owner)
    public BookingResponse cancelBooking(int bookingId, String role) {
        // Gọi Service để xử lý logic hủy đơn (đổi trạng thái thành CANCELLED và tính tiền hoàn trả)
        BookingResponse cancelledBooking = bookingService.cancelBooking(bookingId, role);

        // Cập nhật lại trạng thái xe thành AVAILABLE (Sẵn sàng cho người khác thuê)
        // Dùng getVehicleId() thay vì cố gắng gọi getVehicleStatus() bị lỗi
        vehicleService.updateVehicleStatus(cancelledBooking.getVehicleId(), VehicleStatus.AVAILABLE);

        // Tiến hành hoàn tiền nếu số tiền hoàn trả lớn hơn 0
        if (cancelledBooking.getRefundAmount() > 0) {
            // Đã sửa lỗi: Truyền đủ 2 tham số (số tiền, mã booking) cho PaymentService
            paymentService.refund(cancelledBooking.getRefundAmount(), bookingId);
        }

        return cancelledBooking; // Trả về DTO để Controller phản hồi cho người dùng
    }

    // 3. Hoàn thành chuyến đi
    public void completeBooking(int bookingId) {
        // Lấy thông tin booking hiện tại
        BookingResponse booking = bookingService.getBookingById(bookingId);

        // Cập nhật trạng thái booking thành COMPLETED
        bookingService.completeBooking(bookingId);

        // Cập nhật trạng thái xe thành AVAILABLE
        vehicleService.updateVehicleStatus(booking.getVehicleId(), VehicleStatus.AVAILABLE);
    }

    // 4. Chủ xe xác nhận đơn đặt
    public void ownerConfirmBooking(int bookingId) {
        // Lấy thông tin booking
        BookingResponse booking = bookingService.getBookingById(bookingId);

        // Cập nhật trạng thái booking thành CONFIRMED
        bookingService.approveBooking(bookingId);

        // Cập nhật trạng thái xe thành BOOKED (Đã được đặt, không ai được đặt đè lên)
        vehicleService.updateVehicleStatus(booking.getVehicleId(), VehicleStatus.BOOKED);
    }
}