package com.carrental.service;

import com.carrental.DTO.BookingRequest;
import com.carrental.DTO.BookingResponse;
import com.carrental.model.entity.Coupon;
import com.carrental.model.entity.Decorator.*;
import com.carrental.model.entity.Renter;
import com.carrental.model.entity.Vehicle;
import com.carrental.model.enums.BookingStatus;
import com.carrental.repository.BookingRepository;
import com.carrental.repository.CouponRepository;
import com.carrental.repository.UserRepository;
import com.carrental.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.stream.Collectors;

/**
 * Xử lý nghiệp vụ lõi của tính năng Đặt xe.
 */
@Service
@RequiredArgsConstructor
public class BookingService extends Observable {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final CouponRepository couponRepository;

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        // 1. Kiểm tra tính hợp lệ của thời gian
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống.");
        }
        if (request.getEndDate().before(request.getStartDate())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.");
        }

        // 2. Định danh khách thuê và xác thực xe
        Renter renter = (Renter) userRepository.findById(request.getRenterId())
                .filter(Renter.class::isInstance)
                .map(Renter.class::cast)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người thuê hợp lệ với ID: " + request.getRenterId()));

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy xe với ID: " + request.getVehicleId()));

        // 3. Đảm bảo xe không bị trùng lịch (Chỉ query các đơn có status đang active)
        List<Booking> overlaps = bookingRepository.findOverlappingBookings(
                request.getVehicleId(), request.getStartDate(), request.getEndDate());
        if (!overlaps.isEmpty()) {
            throw new IllegalStateException("Xe đã được đặt hoặc thuê trong khoảng thời gian này.");
        }

        // 4. Tính toán giá trị thuê cơ bản theo ngày
        long diffMs = request.getEndDate().getTime() - request.getStartDate().getTime();
        long days = (diffMs / (24 * 60 * 60 * 1000)) + 1;
        double baseAmount = Math.max(days, 1) * vehicle.getPricePerDay();

        // 5. Khởi tạo Context cho Decorator
        Booking booking = new Booking();
        booking.setBookingDate(new Date());
        booking.setStartDate(request.getStartDate());
        booking.setEndDate(request.getEndDate());
        booking.setTotalAmount(baseAmount);
        booking.setRenter(renter);
        booking.setVehicle(vehicle);
        booking.setHasPet(request.isHasPet());
        booking.setHasGPS(request.isHasGPS());
        booking.setHasBabySeat(request.isHasBabySeat());
        booking.setHasDashcam(request.isHasDashcam());

        // 6. Áp dụng Decorator Pattern để tính tổng phụ phí và khuyến mãi
        BookingOrder finalOrder = booking;
        if (request.isHasPet()) finalOrder = new PetDecorator(finalOrder, 150000.0);
        if (request.isHasGPS()) finalOrder = new GPSDecorator(finalOrder, 50000.0);
        if (request.isHasBabySeat()) finalOrder = new BabySeatDecorator(finalOrder, 100000.0);
        if (request.isHasDashcam()) finalOrder = new DashcamDecorator(finalOrder, 80000.0);

        if (request.getCouponCode() != null && !request.getCouponCode().trim().isEmpty()) {
            Coupon coupon = couponRepository.findByCode(request.getCouponCode().trim());
            if (coupon != null) {
                finalOrder = new CouponDecorator(finalOrder, coupon);
            } else {
                throw new IllegalArgumentException("Mã giảm giá không tồn tại hoặc đã hết hạn.");
            }
        }

        // 7. Chốt giá trị hóa đơn và tính tiền cọc (30%)
        double finalAmount = finalOrder.calculateTotal();
        booking.setTotalAmount(finalAmount);
        booking.setDepositAmount(finalAmount * 0.30);

        Booking savedBooking = bookingRepository.save(booking);

        // 8. Kích hoạt Observer Pattern để gửi thông báo cho Chủ xe
        if (booking.getVehicle() != null && booking.getVehicle().getOwner() != null) {
            com.carrental.observer.OwnerObserver.triggerNotification(booking.getVehicle().getOwner().getUserId());
        }

        return mapToResponse(savedBooking);
    }

    @Transactional
    public BookingResponse approveBooking(int bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn: " + bookingId));
        booking.restoreStateFromEnum();
        booking.confirm();
        booking.setConfirmedAt(new Date());
        return mapToResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse cancelBooking(int bookingId, String role) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn: " + bookingId));

        // Chủ xe không được hủy khi đang chờ khách cọc để tránh thao tác sai
        if ("OWNER".equalsIgnoreCase(role) && booking.getBookingStatus() == BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Chủ xe không thể hủy đơn khi đang chờ khách cọc.");
        }

        booking.setRefundAmount(0);

        // Logic hoàn tiền chỉ áp dụng nếu khách đã nộp cọc
        if (booking.getBookingStatus() == BookingStatus.DEPOSIT_PAID) {
            double deposit = booking.getDepositAmount();

            if ("OWNER".equalsIgnoreCase(role)) {
                // Chủ xe hủy: Hoàn 100% tiền cọc cho khách
                booking.setRefundAmount(deposit);
            } else {
                // Khách hủy: Tính toán dựa trên khoảng thời gian còn lại trước khi nhận xe
                long diffMs = booking.getStartDate().getTime() - System.currentTimeMillis();
                long hours = diffMs / (1000 * 60 * 60);

                if (hours > 48) {
                    booking.setRefundAmount(deposit);         // Hủy sớm: Hoàn 100%
                } else if (hours > 24) {
                    booking.setRefundAmount(deposit * 0.5);   // Hủy sát ngày: Hoàn 50%
                } else {
                    booking.setRefundAmount(0);               // Hủy khẩn cấp: Mất cọc
                }
            }
        }

        booking.restoreStateFromEnum();
        booking.cancel();
        return mapToResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse payDeposit(int bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn: " + bookingId));
        booking.restoreStateFromEnum();
        booking.payDeposit();
        return mapToResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse pickUpVehicle(int bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn: " + bookingId));
        booking.restoreStateFromEnum();
        booking.pickUpVehicle();
        return mapToResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse returnVehicle(int bookingId, double lateFee, double damageFee) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn: " + bookingId));

        // Ghi nhận các khoản phụ phí phát sinh lúc trả xe
        booking.setLateFee(lateFee);
        booking.setDamageFee(damageFee);

        booking.restoreStateFromEnum();
        booking.returnVehicle();
        return mapToResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse completeBooking(int bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn: " + bookingId));
        booking.restoreStateFromEnum();
        booking.complete();
        return mapToResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse rejectBooking(int bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn: " + bookingId));
        booking.restoreStateFromEnum();
        booking.cancel();
        return mapToResponse(bookingRepository.save(booking));
    }

    // Cron job: Dọn dẹp các đơn hàng ảo (đã xác nhận nhưng khách không thanh toán cọc sau 12 giờ)
    @Scheduled(fixedRateString = "${scheduler.autoCancelUnpaidBookingsRate:3600000}")
    @Transactional
    public void autoCancelUnpaidBookings() {
        Date threshold = new Date(System.currentTimeMillis() - (12 * 60 * 60 * 1000));

        // Truy vấn trực tiếp các đơn quá hạn
        List<Booking> overdueBookings =
                bookingRepository.findAll().stream().filter(b ->
                        BookingStatus.CONFIRMED.equals(b.getBookingStatus())
                                && b.getConfirmedAt() != null && b.getConfirmedAt().before(threshold))
                        .collect(Collectors.toList());
        for (Booking b : overdueBookings) {
            b.restoreStateFromEnum();
            b.cancel();
        }

        // Lưu hàng loạt (Batch update)
        bookingRepository.saveAll(overdueBookings);
    }

    public BookingResponse updateBookingStatus(int bookingId, BookingStatus status) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn: " + bookingId));
        booking.setBookingStatus(status);
        return mapToResponse(bookingRepository.save(booking));
    }

    public BookingResponse getBookingById(int bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn: " + bookingId));
        return mapToResponse(booking);
    }

    public List<BookingResponse> getRenterBookings(int renterId) {
        return bookingRepository.findByRenterUserId(renterId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<BookingResponse> getOwnerBookings(int ownerId) {
        return bookingRepository.findByVehicleOwnerUserId(ownerId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
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
        response.setHasPet(booking.isHasPet());
        response.setHasGPS(booking.isHasGPS());
        response.setHasBabySeat(booking.isHasBabySeat());
        response.setHasDashcam(booking.isHasDashcam());

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