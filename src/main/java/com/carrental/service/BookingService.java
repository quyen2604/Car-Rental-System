package com.carrental.service;

import com.carrental.DTO.BookingRequest;
import com.carrental.DTO.BookingResponse;
import com.carrental.model.entity.Booking;
import com.carrental.model.entity.Renter;
import com.carrental.model.entity.User;
import com.carrental.model.entity.Vehicle;
import com.carrental.model.enums.BookingStatus;
import com.carrental.repository.BookingRepository;
import com.carrental.repository.UserRepository;
import com.carrental.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        // 1. Kiểm tra ngày đặt
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống.");
        }
        if (request.getEndDate().before(request.getStartDate())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.");
        }

        // 2. Tìm người thuê
        User user = userRepository.findById(request.getRenterId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.getRenterId()));
        if (!(user instanceof Renter)) {
            throw new IllegalArgumentException("Người dùng không phải là người thuê xe (Renter).");
        }
        Renter renter = (Renter) user;

        // 3. Tìm xe
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy xe với ID: " + request.getVehicleId()));

        // 4. Kiểm tra trùng lịch đặt
        List<Booking> overlaps = bookingRepository.findOverlappingBookings(
                request.getVehicleId(), request.getStartDate(), request.getEndDate());
        if (!overlaps.isEmpty()) {
            throw new IllegalStateException("Xe đã được đặt hoặc thuê trong khoảng thời gian này.");
        }

        // 5. Tính tổng tiền
        long diff = request.getEndDate().getTime() - request.getStartDate().getTime();
        long days = diff / (24 * 60 * 60 * 1000);
        if (days <= 0) {
            days = 1; // Tối thiểu tính 1 ngày thuê
        }
        double totalAmount = days * vehicle.getPricePerDay();

        // 6. Lưu Booking
        Booking booking = new Booking();
        booking.setBookingDate(new Date());
        booking.setStartDate(request.getStartDate());
        booking.setEndDate(request.getEndDate());
        booking.setTotalAmount(totalAmount);
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setRenter(renter);
        booking.setVehicle(vehicle);

        Booking savedBooking = bookingRepository.save(booking);
        return mapToResponse(savedBooking);
    }

    @Transactional
    public BookingResponse updateBookingStatus(int bookingId, BookingStatus status) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt xe với ID: " + bookingId));
        booking.setBookingStatus(status);
        Booking savedBooking = bookingRepository.save(booking);
        return mapToResponse(savedBooking);
    }

    @Transactional
    public BookingResponse cancelBooking(int bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt xe với ID: " + bookingId));
        if (booking.getBookingStatus() != BookingStatus.PENDING && booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Chỉ có thể hủy đơn đặt xe ở trạng thái PENDING hoặc CONFIRMED.");
        }
        booking.setBookingStatus(BookingStatus.CANCELLED);
        Booking savedBooking = bookingRepository.save(booking);
        return mapToResponse(savedBooking);
    }

    public List<BookingResponse> getRenterBookings(int renterId) {
        List<Booking> bookings = bookingRepository.findByRenterUserId(renterId);
        return bookings.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public BookingResponse getBookingById(int bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt xe với ID: " + bookingId));
        return mapToResponse(booking);
    }

    private BookingResponse mapToResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getBookingId());
        response.setBookingDate(booking.getBookingDate());
        response.setStartDate(booking.getStartDate());
        response.setEndDate(booking.getEndDate());
        response.setTotalAmount(booking.getTotalAmount());
        response.setBookingStatus(booking.getBookingStatus().name());

        if (booking.getRenter() != null) {
            response.setRenterId(booking.getRenter().getUserId());
            response.setRenterName(booking.getRenter().getFullName());
        }

        if (booking.getVehicle() != null) {
            response.setVehicleId(booking.getVehicle().getVehicleId());
            response.setVehicleBrand(booking.getVehicle().getBrand());
            response.setVehicleModel(booking.getVehicle().getModel());
            response.setLicensePlate(booking.getVehicle().getLicensePlate());
            response.setPricePerDay(booking.getVehicle().getPricePerDay());
        }

        return response;
    }
}
