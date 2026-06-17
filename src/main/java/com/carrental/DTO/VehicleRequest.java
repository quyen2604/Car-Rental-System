package com.carrental.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRequest {
    private String type;           // "CAR" hoặc "MOTORBIKE"
    private String brand;
    private String model;
    private String licensePlate;
    private double pricePerDay;
    private String description;
    private int seatNumber;        // Chỉ dùng khi type = CAR
    private int engineCapacity;    // Chỉ dùng khi type = MOTORBIKE
    private int ownerId;

    // Địa điểm
    private String city;
    private String district;
    private String addressDetail;
}
