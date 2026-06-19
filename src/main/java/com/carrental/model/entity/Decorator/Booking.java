package com.carrental.model.entity.Decorator;

import com.carrental.model.entity.Renter;
import com.carrental.model.entity.Vehicle;
import com.carrental.model.enums.BookingStatus;
import com.carrental.model.state.*;
import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

/**
 * Quản lý thông tin và trạng thái của một đơn đặt xe.
 * - State Pattern: Chuyển đổi trạng thái đơn hàng (Pending -> Confirmed -> Renting...).
 * - Decorator Pattern: Linh hoạt tính toán tổng tiền khi thêm các dịch vụ đi kèm.
 */
@Entity
@Table(name = "bookings")
@Data
@AllArgsConstructor
public class Booking implements BookingOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int bookingId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date bookingDate;

    // Sử dụng TIMESTAMP để lưu chính xác giờ/phút/giây, hỗ trợ tính toán thời gian hủy đơn chính xác
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    // Tổng tiền hợp đồng ban đầu (Giá thuê cơ bản + Phụ phí dịch vụ - Khuyến mãi)
    private double totalAmount;

    // Số tiền cọc bắt buộc khách phải thanh toán trước khi nhận xe
    private double depositAmount;

    // Số tiền hệ thống sẽ hoàn trả tùy thuộc vào thời điểm và người chủ động hủy đơn
    private double refundAmount;

    // Các khoản phí phát sinh thực tế được ghi nhận độc lập lúc trả xe
    @Column(columnDefinition = "double default 0")
    private double lateFee;

    @Column(columnDefinition = "double default 0")
    private double damageFee;

    private boolean hasPet;
    private boolean hasGPS;
    private boolean hasBabySeat;
    private boolean hasDashcam;

    // Mốc thời gian chủ xe duyệt đơn, dùng làm cơ sở cho Scheduler tự động hủy nếu khách không cọc
    @Temporal(TemporalType.TIMESTAMP)
    private Date confirmedAt;

    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;

    @ManyToOne
    @JoinColumn(name = "renter_id")
    private Renter renter;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    // Đối tượng quản lý luồng trạng thái, chỉ tồn tại ở runtime (không lưu xuống database)
    @Transient
    private BookingState state;

    public Booking() {
        this.state = new PendingState();
        this.bookingStatus = BookingStatus.PENDING;
    }

    // Tự động khôi phục State object tương ứng dựa trên giá trị Enum sau khi load từ Database
    @PostLoad
    public void restoreStateFromEnum() {
        if (bookingStatus == null) return;
        switch (bookingStatus) {
            case PENDING -> this.state = new PendingState();
            case CONFIRMED -> this.state = new ConfirmedState();
            case DEPOSIT_PAID -> this.state = new DepositPaidState();
            case RENTING -> this.state = new RentingState();
            case RETURNED -> this.state = new ReturnedState();
            case COMPLETED -> this.state = new CompletedState();
            case CANCELLED -> this.state = new CancelledState();
            default -> this.state = new PendingState();
        }
    }

    public BookingState getState() {
        if (this.state == null) {
            this.state = new PendingState();
            this.bookingStatus = BookingStatus.PENDING;
        }
        return this.state;
    }

    public void setState(BookingState state) {
        this.state = state;
    }

    // --- Các hàm Delegate chuyển tiếp yêu cầu xử lý trạng thái cho State object ---
    public void confirm() { getState().confirm(this); }
    public void payDeposit() { getState().payDeposit(this); }
    public void pickUpVehicle() { getState().pickUpVehicle(this); }
    public void returnVehicle() { getState().returnVehicle(this); }
    public void complete() { getState().complete(this); }
    public void cancel() { getState().cancel(this); }

    // Tính số tiền cuối cùng khách phải trả sau chuyến đi
    public double calculateFinalPaymentRequired() {
        return (totalAmount + lateFee + damageFee) - depositAmount;
    }

    @Override
    public double calculateTotal() {
        return totalAmount;
    }

    @Override
    public String getDescription() {
        return "Đơn đặt xe cơ bản";
    }
}