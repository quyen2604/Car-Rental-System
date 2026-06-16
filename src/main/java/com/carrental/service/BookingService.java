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
        // 1. Kiểm tra ngày đặt
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống.");
        }
        if (request.getEndDate().before(request.getStartDate())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.");
        }

        // 2. Tìm người thuê
        User user = userRepository.findById(request.getRenterId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy người dùng với ID: " + request.getRenterId()));
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
    public BookingResponse approveBooking(int bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt xe với ID: " + bookingId));

        // Khôi phục lại State object từ Enum dưới DB
        booking.restoreStateFromEnum();

        // Gọi ủy quyền confirm. Nếu không hợp lệ sẽ tự văng lỗi từ AbstractBookingState.
        booking.confirm();
        
        // Ghi nhận thời gian xác nhận để tính tự động hủy sau 12h
        booking.setConfirmedAt(new Date());

        Booking savedBooking = bookingRepository.save(booking);
        return mapToResponse(savedBooking);
    }

    @Transactional
    public BookingResponse cancelBooking(int bookingId, String role) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt xe với ID: " + bookingId));

        if ("OWNER".equalsIgnoreCase(role) && booking.getBookingStatus() == BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Chủ xe không thể hủy đơn khi đang chờ khách cọc.");
        }

        // Tính hoàn cọc nếu đơn đã ở trạng thái DEPOSIT_PAID
        if (booking.getBookingStatus() == BookingStatus.DEPOSIT_PAID || booking.getBookingStatus() == BookingStatus.CONFIRMED) {
            double deposit = booking.getTotalAmount(); // Giả sử cọc = 100% totalAmount cho đơn giản, hoặc có thể custom
            if ("OWNER".equalsIgnoreCase(role)) {
                // Owner hủy: Hoàn 100%
                booking.setRefundAmount(deposit);
                System.out.println("Owner hủy, hoàn tiền cọc 100%: " + deposit);
            } else {
                // Renter hủy
                long diffMs = booking.getStartDate().getTime() - System.currentTimeMillis();
                long hours = diffMs / (1000 * 60 * 60);
                
                if (hours > 48) {
                    booking.setRefundAmount(deposit); // 100%
                    System.out.println("Renter hủy trước >48h, hoàn 100%: " + deposit);
                } else if (hours > 24) {
                    booking.setRefundAmount(deposit * 0.5); // 50%
                    System.out.println("Renter hủy trước 24-48h, hoàn 50%: " + (deposit * 0.5));
                } else {
                    booking.setRefundAmount(0); // 0%
                    System.out.println("Renter hủy trước <24h, hoàn 0%");
                }
            }
        }

        // Khôi phục lại State object từ Enum dưới DB
        booking.restoreStateFromEnum();

        // Gọi ủy quyền cancel. Nếu không hợp lệ sẽ tự văng lỗi từ AbstractBookingState.
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

        booking.restoreStateFromEnum();
        booking.returnVehicle(lateFee, damageFee);

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
        
        // Khôi phục lại State object từ Enum dưới DB
        booking.restoreStateFromEnum();
        
        // Gọi ủy quyền cancel. Nếu không hợp lệ sẽ tự văng lỗi từ AbstractBookingState.
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

    @Scheduled(fixedRate = 3600000) // Chạy mỗi 1 giờ
    @Transactional
    public void autoCancelUnpaidBookings() {
        System.out.println("Scheduler: Quét các đơn hàng quá 12h chưa cọc...");
        List<Booking> allBookings = bookingRepository.findAll();
        
        // Tìm các đơn CONFIRMED quá 12h
        List<Booking> overdueBookings = allBookings.stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED && b.getConfirmedAt() != null)
                .filter(b -> {
                    long diffMs = System.currentTimeMillis() - b.getConfirmedAt().getTime();
                    long hours = diffMs / (1000 * 60 * 60);
                    return hours >= 12;
                })
                .collect(Collectors.toList());

        for (Booking b : overdueBookings) {
            System.out.println("Scheduler: Tự động hủy Booking ID " + b.getBookingId() + " do quá hạn cọc.");
            b.restoreStateFromEnum();
            b.cancel();
            bookingRepository.save(b);
        }
    }

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
