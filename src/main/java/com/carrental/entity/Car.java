package com.carrental.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cars")
@PrimaryKeyJoinColumn(name = "vehicle_id")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Car extends Vehicle {

    @Column(name = "seat_count", nullable = false)
    private Integer seatCount = 4;

    @Column(nullable = false)
    private String transmission = "AUTOMATIC"; // AUTOMATIC hoặc MANUAL
}
