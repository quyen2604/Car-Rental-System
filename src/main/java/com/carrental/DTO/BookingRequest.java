package com.carrental.DTO;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

@Data
public class BookingRequest {
    private int renterId;
    private int vehicleId;

    // Đổi pattern thành yyyy-MM-dd để khớp với dữ liệu thực tế trình duyệt gửi ngầm
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Ho_Chi_Minh")
    private Date startDate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Ho_Chi_Minh")
    private Date endDate;
    private String couponCode;
    private double deposit;
}