package com.carrental.model.entity;
import com.carrental.model.enums.ApprovalStatus;
import com.carrental.model.enums.VehicleStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicles")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int vehicleId;

    private String brand;
    private String model;
    private String licensePlate;
    private double pricePerDay;
    private String description;

    @Enumerated(EnumType.STRING)
    private VehicleStatus vehicleStatus;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private Owner owner;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;
}