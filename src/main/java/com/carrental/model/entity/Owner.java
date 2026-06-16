package com.carrental.model.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "owners")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Owner extends User {
    // Sơ đồ không có thuộc tính riêng, giữ nguyên để phân hệ
}