package com.carrental.model.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "renters")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Renter extends User {
    private String licenseNumber;
}