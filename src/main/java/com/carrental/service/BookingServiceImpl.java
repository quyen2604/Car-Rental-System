package com.carrental.service;

import com.carrental.entity.Booking;
import com.carrental.entity.BookingStatus;
import com.carrental.entity.User;
import com.carrental.entity.Vehicle;
import com.carrental.entity.Payment;
import com.carrental.repository.BookingRepository;
import com.carrental.repository.UserRepository;
import com.carrental.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final UserService userService;

    @Override
    @Transactional
    public Booking createBooking(Long renterId, Long vehicleId, LocalDate startDate, LocalDate endDate, String note,
                                 String guestFullName, String guestEmail, String guestPhone,
                                 String paymentId, String paymentMethod, Double paymentAmount) {
        // Validate dates
        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày nhận xe không thể ở quá khứ");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Ngày trả xe phải bằng hoặc sau ngày nhận xe");
        }

        // Get or create renter
        User renter;
        if (renterId != null) {
            renter = userRepository.findById(renterId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người thuê với ID: " + renterId));
        } else {
            if (guestEmail == null || guestEmail.isBlank() || guestFullName == null || guestFullName.isBlank()) {
                throw new IllegalArgumentException("Vui lòng đăng nhập hoặc nhập đầy đủ thông tin Họ tên & Email của bạn!");
            }
            renter = userService.getOrCreateGuestUser(guestEmail, guestFullName, guestPhone);
        }

        // Check if vehicle exists
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy xe với ID: " + vehicleId));

        // Check availability
        if (!isVehicleAvailable(vehicleId, startDate, endDate)) {
            throw new IllegalStateException("Xe đã bị đặt trùng lịch trong khoảng thời gian này");
        }

        // Calculate pricing
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1; // Tính cả ngày nhận và ngày trả
        double totalPrice = vehicle.getPricePerDay() * days;
        double depositPaid = totalPrice * 0.3;

        // Create booking
        Booking booking = Booking.builder()
                .renter(renter)
                .vehicle(vehicle)
                .startDate(startDate)
                .endDate(endDate)
                .totalPrice(totalPrice)
                .depositPaid(depositPaid)
                .bookingStatus(BookingStatus.PENDING)
                .note(note)
                .build();
        // Create payment record (UML association)
        if (paymentId != null && !paymentId.isBlank()) {
            Payment payment = Payment.builder()
                    .paymentId(paymentId)
                    .booking(booking)
                    .amount(paymentAmount != null ? paymentAmount : depositPaid)
                    .method(paymentMethod != null ? paymentMethod : "BANK_TRANSFER")
                    .status("PENDING")
                    .build();
            booking.getPaymentList().add(payment);
        } else {
            throw new IllegalArgumentException("Vui lòng thực hiện chuyển khoản và cung cấp mã giao dịch thanh toán để tiếp tục đặt xe!");
        }

        return bookingRepository.save(booking);
    }

    @Override
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt xe với ID: " + id));
    }

    @Override
    public List<Booking> getBookingsByRenter(Long renterId) {
        return bookingRepository.findByRenterId(renterId);
    }

    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    @Transactional
    public Booking updateStatus(Long id, String status) {
        Booking booking = getBookingById(id);
        try {
            BookingStatus newStatus = BookingStatus.valueOf(status.toUpperCase());
            booking.setBookingStatus(newStatus);
            return bookingRepository.save(booking);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + status);
        }
    }

    @Override
    public boolean isVehicleAvailable(Long vehicleId, LocalDate startDate, LocalDate endDate) {
        List<Booking> overlapping = bookingRepository.findOverlappingBookings(vehicleId, startDate, endDate);
        return overlapping.isEmpty();
    }
}
