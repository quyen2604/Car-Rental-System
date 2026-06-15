package com.carrental.controller;


import com.carrental.model.entity.Car;
import com.carrental.model.entity.Motorbike;
import com.carrental.model.entity.Vehicle;
import com.carrental.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    @Autowired
    private VehicleRepository vehicleRepository;

    //tìm kiếm xe
    @GetMapping("/search-vehicle")
    public List<Vehicle> searchVehicle(@RequestParam String city, @RequestParam String type) {
        // 1. Lấy tất cả các xe đang trống (AVAILABLE) tại thành phố đó
        List<Vehicle> allVehiclesInCity = vehicleRepository.findByLocation_City(city);
        return allVehiclesInCity.stream()
                .filter( v -> {
                    if ("MOTORBIKE".equalsIgnoreCase(type)) {
                        return v instanceof Motorbike; // Chỉ lấy xe máy
                    }
                    return v instanceof Car;
                }).collect(Collectors.toList());
    }

    // Lấy thông tin chi tiết của 1 xe
    @GetMapping("/{id}")
    public org.springframework.http.ResponseEntity<Vehicle> getVehicleById(@PathVariable int id) {
        return vehicleRepository.findById(id)
                .map(org.springframework.http.ResponseEntity::ok)
                .orElse(org.springframework.http.ResponseEntity.notFound().build());
    }
}
