package com.carrental.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "motorbikes")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Motorbike extends Vehicle {
    private int engineCapacity;
}