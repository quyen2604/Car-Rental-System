package com.carrental.model.entity;

import com.carrental.model.enums.BookingStatus;
import com.carrental.model.state.BookingState;
import com.carrental.model.state.*;
import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "bookings")
@Data
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int bookingId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date bookingDate;

    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Temporal(TemporalType.DATE)
    private Date endDate;

    private double totalAmount;

    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;

    @ManyToOne
    @JoinColumn(name = "renter_id")
    private Renter renter;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Transient
    private BookingState state = new PendingState();

    public Booking() {
        this.state = new PendingState();
        this.bookingStatus = BookingStatus.PENDING;
    }

    @PostLoad
    public void restoreStateFromEnum() {
        if (bookingStatus == null) {
            return;
        }
        switch (bookingStatus) {
            case PENDING:
                this.state = new PendingState();
                break;
            case CONFIRMED:
                this.state = new ConfirmedState();
                break;
            case DEPOSIT_PAID:
                this.state = new DepositPaidState();
                break;
            case RENTING:
                this.state = new RentingState();
                break;
            case RETURNED:
                this.state = new ReturnedState();
                break;
            case COMPLETED:
                this.state = new CompletedState();
                break;
            case CANCELLED:
                this.state = new CancelledState();
                break;
            default:
                this.state = new PendingState();
                break;
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

    public void confirm() {
        getState().confirm(this);
    }

    public void payDeposit() {
        getState().payDeposit(this);
    }

    public void pickUpVehicle() {
        getState().pickUpVehicle(this);
    }

    public void returnVehicle(double lateFee, double damageFee) {
        getState().returnVehicle(this, lateFee, damageFee);
    }

    public void complete() {
        getState().complete(this);
    }

    public void cancel() {
        getState().cancel(this);
    }
}