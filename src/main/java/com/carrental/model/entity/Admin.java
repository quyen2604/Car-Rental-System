package com.carrental.model.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admins")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Admin extends User {
}