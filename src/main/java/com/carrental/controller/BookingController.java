package com.carrental.controller;

import com.carrental.DTO.BookingRequest;
import com.carrental.DTO.BookingResponse;
import com.carrental.facade.BookingFacade;
import com.carrental.model.enums.BookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Quan trọng: Cho phép Web gọi API không bị lỗi chặn
public class BookingController {

    private final BookingFacade bookingFacade;

    // BƯỚC 1: Chỉ tạo đơn (Khách hàng bấm "Đặt Xe Ngay")
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {
        try {
            BookingResponse response = bookingFacade.createBookingOnly(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // BƯỚC 2: Chủ xe duyệt đơn
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> approveBooking(@PathVariable int id) {
        try {
            BookingResponse response = bookingFacade.ownerConfirmBooking(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // BƯỚC 3: Khách hàng thanh toán cọc
    // amount và method là tuỳ chọn — nếu không gửi thì dùng giá trị mặc định (0% & MOMO)
    @PostMapping("/{id}/pay-deposit")
    public ResponseEntity<?> payDeposit(
            @PathVariable int id,
            @RequestParam(required = false, defaultValue = "0") double amount,
            @RequestParam(required = false, defaultValue = "MOMO") String method) {
        try {
            BookingResponse response = bookingFacade.payDepositForBooking(id, amount, method);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // BƯỚC 4: Khách hàng nhận xe
    @PostMapping("/{id}/pick-up")
    public ResponseEntity<?> pickUpVehicle(@PathVariable int id) {
        try {
            BookingResponse response = bookingFacade.pickUpVehicle(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // BƯỚC 5: Khách hàng trả xe (Tính thêm phí muộn, phí hỏng hóc nếu có)
    @PostMapping("/{id}/return")
    public ResponseEntity<?> returnVehicle(
            @PathVariable int id,
            @RequestParam(defaultValue = "0") double lateFee,
            @RequestParam(defaultValue = "0") double damageFee) {
        try {
            BookingResponse response = bookingFacade.returnVehicle(id, lateFee, damageFee);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // BƯỚC 6: Khách hàng thanh toán nốt phần còn lại
    // amount và method là tuỳ chọn — frontend mới dùng /api/payments/final thay thế
    @PostMapping("/{id}/pay-remaining")
    public ResponseEntity<?> payRemaining(
            @PathVariable int id,
            @RequestParam(required = false, defaultValue = "0") double amount,
            @RequestParam(required = false, defaultValue = "MOMO") String method) {
        try {
            bookingFacade.payRemainingBalance(id, amount, method);
            return ResponseEntity.ok("Thanh toán phần còn lại thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // BƯỚC 7: Hoàn tất đơn hàng
    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeBooking(@PathVariable int id) {
        try {
            BookingResponse response = bookingFacade.completeBooking(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- CÁC API TIỆN ÍCH ---

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable int id, @RequestParam(required = false, defaultValue = "RENTER") String role) {
        try {
            BookingResponse response = bookingFacade.cancelBooking(id, role);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> rejectBooking(@PathVariable int id) {
        try {
            BookingResponse response = bookingFacade.rejectBooking(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable int id) {
        try {
            BookingResponse response = bookingFacade.getBookingById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/renter/{renterId}")
    public ResponseEntity<List<BookingResponse>> getRenterBookings(@PathVariable int renterId) {
        return ResponseEntity.ok(bookingFacade.getRenterBookings(renterId));
    }

    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<BookingResponse>> getOwnerBookings(@PathVariable int ownerId) {
        return ResponseEntity.ok(bookingFacade.getOwnerBookings(ownerId));
    }
}