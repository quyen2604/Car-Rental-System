package com.carrental.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; // Thêm cái này để có constructor rỗng

@Entity
@Table(name = "coupons")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private double percent;
}