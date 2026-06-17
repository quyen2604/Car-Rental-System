package com.carrental.service;

import com.carrental.DTO.BookingRequest;
import com.carrental.DTO.BookingResponse;
import com.carrental.model.entity.Booking;
import com.carrental.model.entity.Renter;
import com.carrental.model.entity.Vehicle;
import com.carrental.model.enums.BookingStatus;
import com.carrental.repository.BookingRepository;
import com.carrental.repository.UserRepository;
import com.carrental.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
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
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống.");
        }
        if (request.getEndDate().before(request.getStartDate())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.");
        }

        // Đã sửa lỗi khai báo trùng biến renter
        Renter renter = (Renter) userRepository.findById(request.getRenterId())
                .filter(Renter.class::isInstance)
                .map(Renter.class::cast)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy người thuê hợp lệ với ID: " + request.getRenterId()));

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy xe với ID: " + request.getVehicleId()));

        // 4. Kiểm tra trùng lịch đặt
        List<Booking> overlaps = bookingRepository.findOverlappingBookings(
                request.getVehicleId(), request.getStartDate(), request.getEndDate());
        if (!overlaps.isEmpty()) {
            throw new IllegalStateException("Xe đã được đặt hoặc thuê trong khoảng thời gian này.");
        }

        long diff = request.getEndDate().getTime() - request.getStartDate().getTime();
        long days = diff / (24 * 60 * 60 * 1000);
        if (days <= 0) {
            days = 1;
        }
        double totalAmount = days * vehicle.getPricePerDay();

        Booking booking = new Booking();
        booking.setBookingDate(new Date());
        booking.setStartDate(new Date(request.getStartDate().getTime()));
        booking.setEndDate(new Date(request.getEndDate().getTime()));
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
    public BookingResponse approveBooking(int bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt xe với ID: " + bookingId));

        booking.restoreStateFromEnum();
        booking.confirm();
        booking.setConfirmedAt(new Date(System.currentTimeMillis()));

        Booking savedBooking = bookingRepository.save(booking);
        return mapToResponse(savedBooking);
    }

    @Transactional
    public BookingResponse cancelBooking(int bookingId, String role) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Booking với ID: " + bookingId));

        if ("OWNER".equalsIgnoreCase(role) && booking.getBookingStatus() == BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Chủ xe không thể hủy đơn khi đang chờ khách cọc.");
        }

        if (List.of(BookingStatus.DEPOSIT_PAID, BookingStatus.CONFIRMED).contains(booking.getBookingStatus())) {
            double deposit = booking.getTotalAmount();
            if ("OWNER".equalsIgnoreCase(role)) {
                booking.setRefundAmount(deposit);
                System.out.println("Owner hủy, hoàn tiền cọc 100%: " + deposit);
            } else {
                long diffMs = booking.getStartDate().getTime() - System.currentTimeMillis();
                long hours = diffMs / (1000 * 60 * 60);
                if (hours > 48) {
                    booking.setRefundAmount(deposit);
                    System.out.println("Renter hủy trước >48h, hoàn 100%: " + deposit);
                } else if (hours > 24) {
                    booking.setRefundAmount(deposit * 0.5);
                    System.out.println("Renter hủy trước 24-48h, hoàn 50%: " + (deposit * 0.5));
                } else {
                    booking.setRefundAmount(0);
                    System.out.println("Renter hủy trước <24h, hoàn 0%");
                }
            }
        }

        // Khôi phục lại State object từ Enum dưới DB
        booking.restoreStateFromEnum();
        booking.cancel();

        Booking savedBooking = bookingRepository.save(booking);
        return mapToResponse(savedBooking);
    }

    @Transactional
    public BookingResponse payDeposit(int bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt xe với ID: " + bookingId));

        booking.restoreStateFromEnum();
        booking.payDeposit();

        Booking savedBooking = bookingRepository.save(booking);
        return mapToResponse(savedBooking);
    }

    @Transactional
    public BookingResponse pickUpVehicle(int bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt xe với ID: " + bookingId));

        booking.restoreStateFromEnum();
        booking.pickUpVehicle();

        Booking savedBooking = bookingRepository.save(booking);
        return mapToResponse(savedBooking);
    }

    @Transactional
    public BookingResponse returnVehicle(int bookingId, double lateFee, double damageFee) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt xe với ID: " + bookingId));

        double totalExtraFee = lateFee + damageFee;
        if (totalExtraFee > 0) {
            booking.setTotalAmount(booking.getTotalAmount() + totalExtraFee);
        }

        booking.restoreStateFromEnum();
        booking.returnVehicle();

        Booking savedBooking = bookingRepository.save(booking);
        return mapToResponse(savedBooking);
    }

    @Transactional
        public BookingResponse completeBooking(int bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt xe với ID: " + bookingId));

        booking.restoreStateFromEnum();
        booking.complete();

        Booking savedBooking = bookingRepository.save(booking);
        return mapToResponse(savedBooking);
    }

    @Transactional
    public BookingResponse rejectBooking(int bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt xe với ID: " + bookingId));

        booking.restoreStateFromEnum();
        booking.cancel();

        Booking savedBooking = bookingRepository.save(booking);
        return mapToResponse(savedBooking);
    }

    public List<BookingResponse> getRenterBookings(int renterId) {
        List<Booking> bookings = bookingRepository.findByRenterUserId(renterId);
        return bookings.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<BookingResponse> getOwnerBookings(int ownerId) {
        List<Booking> bookings = bookingRepository.findByVehicleOwnerUserId(ownerId);
        return bookings.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public BookingResponse getBookingById(int bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt xe với ID: " + bookingId));
        return mapToResponse(booking);
    }

    @Scheduled(fixedRateString = "${scheduler.autoCancelUnpaidBookingsRate:3600000}")
    @Transactional
    public void autoCancelUnpaidBookings() {
        System.out.println("Scheduler: Quét các đơn hàng quá 12h chưa cọc...");
        List<Booking> allBookings = bookingRepository.findAll();

        List<Booking> overdueBookings = allBookings.stream()
                .filter(b -> BookingStatus.CONFIRMED.equals(b.getBookingStatus()) && b.getConfirmedAt() != null)
                .filter(b -> {
                    return (System.currentTimeMillis() - b.getConfirmedAt().getTime()) / (1000 * 60 * 60) >= 12;
                })
                .collect(Collectors.toList());

        for (Booking b : overdueBookings) {
            System.out.println("Scheduler: Tự động hủy Booking ID " + b.getBookingId() + " do quá hạn cọc.");
            b.restoreStateFromEnum();
            b.cancel();
            bookingRepository.save(b);
        }
    }

    // Đã thêm vỏ hàm private BookingResponse mapToResponse(Booking booking) để bọc các dòng code bị lỗi
    private BookingResponse mapToResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getBookingId());
        response.setBookingDate(booking.getBookingDate());
        response.setStartDate(booking.getStartDate());
        response.setEndDate(booking.getEndDate());
        response.setTotalAmount(booking.getTotalAmount());
        response.setRefundAmount(booking.getRefundAmount());
        response.setBookingStatus(booking.getBookingStatus().name());

        if (booking.getRenter() != null) {
            response.setRenterId(booking.getRenter().getUserId());
            response.setRenterName(booking.getRenter().getFullName());
        }

        if (booking.getVehicle() != null && booking.getVehicle().getOwner() != null) {
            response.setVehicleId(booking.getVehicle().getVehicleId());
            response.setVehicleBrand(booking.getVehicle().getBrand());
            response.setVehicleModel(booking.getVehicle().getModel());
            response.setLicensePlate(booking.getVehicle().getLicensePlate());
            response.setPricePerDay(booking.getVehicle().getPricePerDay());
        }

        return response;
    }
}