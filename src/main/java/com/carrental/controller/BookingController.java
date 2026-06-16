package com.carrental.controller;

import com.carrental.DTO.BookingRequest;
import com.carrental.DTO.BookingResponse;
import com.carrental.model.entity.Booking;
import com.carrental.model.enums.BookingStatus;
import com.carrental.service.BookingService;
import com.carrental.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Thêm dòng này để Frontend gọi API không bị lỗi CORS block
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {
        try {
            BookingResponse response = bookingService.createBooking(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateBookingStatus(@PathVariable int id, @RequestParam BookingStatus status) {
        try {
            BookingResponse response = bookingService.updateBookingStatus(id, status);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable int id) {
        try {
            BookingResponse response = bookingService.cancelBooking(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/renter/{renterId}")
    public ResponseEntity<List<BookingResponse>> getRenterBookings(@PathVariable int renterId) {
        List<BookingResponse> responses = bookingService.getRenterBookings(renterId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable int id) {
        try {
            BookingResponse response = bookingService.getBookingById(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Đã xảy ra lỗi: " + e.getMessage());
        }
    }
    @PostMapping("/{bookingId}/approve")
    public ResponseEntity<String> approveBooking(@PathVariable String bookingId) {
        try {
            bookingService.approveBooking(bookingId);
            return ResponseEntity.ok("Đã phê duyệt đơn đặt xe thành công!");
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Mã đơn hàng không đúng định dạng số.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @PostMapping("/{bookingId}/reject")
    public ResponseEntity<String> rejectBooking(@PathVariable String bookingId) {
        try {
            bookingService.rejectBooking(bookingId);
            return ResponseEntity.ok("Đã từ chối đơn đặt xe!");
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Mã đơn hàng không đúng định dạng số.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + e.getMessage());
        }
    }
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<?> getOwnerBookings(@PathVariable int ownerId) {
        try {
            List<BookingResponse> responses = bookingService.getBookingsByOwnerVehicles(ownerId);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi hệ thống khi lấy đơn Owner: " + e.getMessage());
        }
    }
}