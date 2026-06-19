package com.carrental.model.enums;

/**
 * Quản lý trạng thái vòng đời của một chiếc xe trong hệ thống.
 */
public enum VehicleStatus {
    AVAILABLE,   // Xe đang trống, sẵn sàng nhận đơn đặt
    BOOKED,      // Xe đã được chủ xe duyệt đơn, đang chờ khách đến nhận
    RENTING,     // Xe đang được khách sử dụng trong chuyến đi
    MAINTENANCE  // Xe đang được bảo trì, không thể đặt
}