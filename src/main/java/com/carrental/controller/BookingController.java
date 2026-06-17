package com.carrental.controller;

import com.carrental.DTO.BookingRequest;
import com.carrental.DTO.BookingResponse;
import com.carrental.facade.BookingFacade;
import com.carrental.model.enums.BookingStatus;
import com.carrental.model.enums.PaymentType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    // Controller CHỈ phụ thuộc vào Facade
    private final BookingFacade bookingFacade;

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request, @RequestParam double deposit) {
        try {
            BookingResponse response = bookingFacade.createBookingWithPayment(request, deposit, PaymentType.DEPOSIT);
            return ResponseEntity.ok(response); // Trả về thông tin đơn thay vì chỉ chuỗi string
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateBookingStatus(@PathVariable int id, @RequestParam BookingStatus status) {
        try {
            BookingResponse response = bookingFacade.updateBookingStatus(id, status);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable int id, @RequestParam(required = false, defaultValue = "RENTER") String role) {
        try {
            BookingResponse response = bookingFacade.cancelBooking(id, role);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('RENTER')")
    public ResponseEntity<?> completeBooking(@PathVariable int id) {
        try {
            BookingResponse response = bookingFacade.completeBooking(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

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
        List<BookingResponse> responses = bookingFacade.getRenterBookings(renterId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<BookingResponse>> getOwnerBookings(@PathVariable int ownerId) {
        List<BookingResponse> responses = bookingFacade.getOwnerBookings(ownerId);
        return ResponseEntity.ok(responses);
    }
}