package com.carrental.model.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    @Id
    private String locationId;
    private String city;
    private String district;
    private String addressDetail;
}