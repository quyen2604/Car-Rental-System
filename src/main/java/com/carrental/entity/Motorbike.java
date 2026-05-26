package com.carrental.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "motorbikes")
@PrimaryKeyJoinColumn(name = "vehicle_id")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Motorbike extends Vehicle {

    @Column(name = "engine_capacity", nullable = false)
    private Integer engineCapacity;
}
