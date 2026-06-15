package com.carrental.DTO;

import lombok.Data;
import java.util.Date;

@Data
public class BookingRequest {
    private int renterId;
    private int vehicleId;
    private Date startDate;
    private Date endDate;
}
