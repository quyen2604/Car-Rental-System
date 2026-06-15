package com.carrental.model.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected int userId;
    protected String fullName;
    protected String email;
    protected String phone;
    protected String password;
    protected boolean active;
}