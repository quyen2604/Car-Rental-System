package com.carrental.dto;

import com.carrental.entity.VehicleStatus;
import com.carrental.entity.VehicleType;
import lombok.Data;

@Data
public class VehicleSearchRequest {
    private VehicleType vehicleType;
    private String address;
    private VehicleStatus status;
}
