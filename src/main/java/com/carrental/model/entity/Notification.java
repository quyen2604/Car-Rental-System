package com.carrental.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Table(name = "notifications")
@Data // Annotation này của Lombok tự động sinh ra hàm setType, setTitle, setUserId...
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    // CHẮC CHẮN PHẢI CÓ DÒNG NÀY THÌ TRONG CLASS SUBJECT MỚI KHÔNG BỊ LỖI ĐỎ:
    @Column(nullable = false)
    private String type;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt = new Date();

    // Trường hợp dự án của bạn KHÔNG DÙNG Lombok hoặc Lombok bị lỗi không nhận diện,
    // Hãy giữ nguyên @Data ở trên và chèn thêm hàm setType thủ công này vào cuối class:
    public void setType(String type) {
        this.type = type;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }
}