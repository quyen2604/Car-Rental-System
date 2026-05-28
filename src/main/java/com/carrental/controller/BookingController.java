package com.carrental.controller;

import com.carrental.entity.Booking;
import com.carrental.service.BookingService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    @Data
    public static class BookingRequest {
        private Long renterId;
        private Long vehicleId;
        private LocalDate startDate;
        private LocalDate endDate;
        private String note;
        private String guestFullName;
        private String guestEmail;
        private String guestPhone;
        private String paymentId;
        private String paymentMethod;
        private Double paymentAmount;
    }
    // POST /api/bookings - Tạo mới đơn đặt xe
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {
        try {
            Booking booking = bookingService.createBooking(
                    request.getRenterId(),
                    request.getVehicleId(),
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getNote(),
                    request.getGuestFullName(),
                    request.getGuestEmail(),
                    request.getGuestPhone(),
                    request.getPaymentId(),
                    request.getPaymentMethod(),
                    request.getPaymentAmount()
            );
            return ResponseEntity.ok(booking);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    // GET /api/bookings/{id} - Lấy đơn đặt theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    // GET /api/bookings/user/{renterId} - Xem lịch sử đặt xe của renter
    @GetMapping("/user/{renterId}")
    public ResponseEntity<List<Booking>> getBookingsByRenter(@PathVariable Long renterId) {
        return ResponseEntity.ok(bookingService.getBookingsByRenter(renterId));
    }

    // GET /api/bookings - Danh sách tất cả đơn đặt xe (Cho Admin)
    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    // PUT /api/bookings/{id}/status - Cập nhật trạng thái (Dùng cho Admin/Owner duyệt hoặc Renter hủy)
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            Booking updatedBooking = bookingService.updateStatus(id, status);
            return ResponseEntity.ok(updatedBooking);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    // GET /api/bookings/check-availability - Kiểm tra xe có sẵn không
    @GetMapping("/check-availability")
    public ResponseEntity<Boolean> checkAvailability(
            @RequestParam Long vehicleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(bookingService.isVehicleAvailable(vehicleId, startDate, endDate));
    }
}
