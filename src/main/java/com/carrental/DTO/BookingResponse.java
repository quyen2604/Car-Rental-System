package com.carrental.DTO;

import lombok.Data;
import java.util.Date;

@Data
public class BookingResponse {
    private int bookingId;
    private Date bookingDate;
    private Date startDate;
    private Date endDate;
    private double totalAmount;
    private double refundAmount;
    private String bookingStatus;

    private int renterId;
    private String renterName;

    private int vehicleId;
    private String vehicleBrand;
    private String vehicleModel;
    private String licensePlate;
    private double pricePerDay;
}
