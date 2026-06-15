package com.carrental.config;
import com.carrental.model.entity.*;
import com.carrental.model.enums.ApprovalStatus;
import com.carrental.model.enums.BookingStatus;
import com.carrental.model.enums.PaymentType;
import com.carrental.model.enums.VehicleStatus;
import com.carrental.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;

@Component
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    // Dùng @Autowired trực tiếp, không tạo class LocationRepository
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        if (userRepository.count() > 0) {
            return; // Nếu đã có dữ liệu thì không chèn nữa
        }

        System.out.println("=== BẮT ĐẦU KHỞI TẠO DỮ LIỆU MẪU ===");

        // 1. TẠO DỮ LIỆU USER (Giữ nguyên theo sơ đồ lớp)
        Admin admin = new Admin();
        admin.setFullName("Anh Duy Min");
        admin.setEmail("admin@carrental.com");
        admin.setPhone("0123456789");
        admin.setPassword("123");
        admin.setActive(true);
        userRepository.save(admin);

        Owner owner = new Owner();
        owner.setFullName("Trần Xe");
        owner.setEmail("owner@gmail.com");
        owner.setPhone("0987654321");
        owner.setPassword("123");
        owner.setActive(true);
        userRepository.save(owner);

        Renter renter = new Renter();
        renter.setFullName("Cần Khách Thuê");
        renter.setEmail("renter@gmail.com");
        renter.setPhone("0909090909");
        renter.setPassword("123");
        renter.setActive(true);
        renter.setLicenseNumber("G1-123456");
        userRepository.save(renter);


        // 2. TẠO ĐỐI TƯỢNG LOCATION (Khởi tạo trực tiếp bằng từ khóa new, không dùng Repository)

        Location locHanoi = new Location();
        locHanoi.setLocationId("LOC_HN");
        locHanoi.setCity("Hanoi");
        locHanoi.setDistrict("Cầu Giấy");
        locHanoi.setAddressDetail("Số 1 Duy Tân");

        Location locHcm = new Location();
        locHcm.setLocationId("LOC_HCM");
        locHcm.setCity("Ho Chi Minh");
        locHcm.setDistrict("Quận 1");
        locHcm.setAddressDetail("100 Lê Lợi");

        Location locDanang = new Location();
        locDanang.setLocationId("LOC_DN");
        locDanang.setCity("Danang");
        locDanang.setDistrict("Hải Châu");
        locDanang.setAddressDetail("50 Bạch Đằng");


        // 3. TẠO DỮ LIỆU VEHICLE (Nét chuẩn theo ảnh)
        // Xe số 1: Car (Hanoi)
        Car car1 = new Car();
        car1.setBrand("Toyota");
        car1.setModel("Camry 2024");
        car1.setLicensePlate("30A-99999");
        car1.setPricePerDay(1200000.0);
        car1.setDescription("Xe sedan sang trọng đời mới, nội thất da cao cấp, phù hợp đi công tác hoặc gặp gỡ đối tác.");
        car1.setVehicleStatus(VehicleStatus.AVAILABLE);
        car1.setApprovalStatus(ApprovalStatus.APPROVED);
        car1.setOwner(owner);
        car1.setSeatNumber(5);
        car1.setLocation(locHanoi);
        vehicleRepository.save(car1);

        // Xe số 2: Car bận lịch (Hanoi)
        Car car2 = new Car();
        car2.setBrand("Hyundai");
        car2.setModel("Accent 2023");
        car2.setLicensePlate("30H-55555");
        car2.setPricePerDay(700000.0);
        car2.setDescription("Xe gia đình nhỏ gọn, tiết kiệm nhiên liệu, vận hành êm ái trong phố cổ.");
        car2.setVehicleStatus(VehicleStatus.AVAILABLE);
        car2.setApprovalStatus(ApprovalStatus.APPROVED);
        car2.setOwner(owner);
        car2.setSeatNumber(5);
        car2.setLocation(locHanoi);
        vehicleRepository.save(car2);

        // Xe số 3: Motorbike (Hanoi)
        Motorbike bike1 = new Motorbike();
        bike1.setBrand("Honda");
        bike1.setModel("Vision 2023");
        bike1.setLicensePlate("29D-88888");
        bike1.setPricePerDay(150000.0);
        bike1.setDescription("Xe ga mini, nhẹ nhàng dễ đi, tiết kiệm xăng tối đa cho mọi hành trình.");
        bike1.setVehicleStatus(VehicleStatus.AVAILABLE);
        bike1.setApprovalStatus(ApprovalStatus.APPROVED);
        bike1.setOwner(owner);
        bike1.setEngineCapacity(110);
        bike1.setLocation(locHanoi);
        vehicleRepository.save(bike1);

        // Xe số 4: Motorbike (Ho Chi Minh)
        Motorbike bike2 = new Motorbike();
        bike2.setBrand("Honda");
        bike2.setModel("SH 150i");
        bike2.setLicensePlate("59F-11111");
        bike2.setPricePerDay(300000.0);
        bike2.setDescription("Xe ga cao cấp, kiểu dáng thể thao lịch lãm, động cơ mạnh mẽ 150cc.");
        bike2.setVehicleStatus(VehicleStatus.AVAILABLE);
        bike2.setApprovalStatus(ApprovalStatus.APPROVED);
        bike2.setOwner(owner);
        bike2.setEngineCapacity(150);
        bike2.setLocation(locHcm);
        vehicleRepository.save(bike2);

        // Xe số 5: Car (Ho Chi Minh)
        Car car3 = new Car();
        car3.setBrand("VinFast");
        car3.setModel("VF8 2024");
        car3.setLicensePlate("51K-77777");
        car3.setPricePerDay(1500000.0);
        car3.setDescription("SUV điện thông minh, vận hành êm ái, trang bị các tính năng an toàn ADAS hiện đại.");
        car3.setVehicleStatus(VehicleStatus.AVAILABLE);
        car3.setApprovalStatus(ApprovalStatus.APPROVED);
        car3.setOwner(owner);
        car3.setSeatNumber(5);
        car3.setLocation(locHcm);
        vehicleRepository.save(car3);

        // Xe số 6: Car (Danang)
        Car car4 = new Car();
        car4.setBrand("Mitsubishi");
        car4.setModel("Xpander 2023");
        car4.setLicensePlate("43A-66666");
        car4.setPricePerDay(900000.0);
        car4.setDescription("Xe 7 chỗ rộng rãi, lý tưởng cho các chuyến du lịch gia đình tại thành phố biển Đà Nẵng.");
        car4.setVehicleStatus(VehicleStatus.AVAILABLE);
        car4.setApprovalStatus(ApprovalStatus.APPROVED);
        car4.setOwner(owner);
        car4.setSeatNumber(7);
        car4.setLocation(locDanang);
        vehicleRepository.save(car4);

        // Xe số 7: Motorbike (Danang)
        Motorbike bike3 = new Motorbike();
        bike3.setBrand("Honda");
        bike3.setModel("Air Blade 2024");
        bike3.setLicensePlate("43K-55555");
        bike3.setPricePerDay(180000.0);
        bike3.setDescription("Xe ga thể thao cá tính, vận hành bền bỉ trên mọi nẻo đường Đà Nẵng.");
        bike3.setVehicleStatus(VehicleStatus.AVAILABLE);
        bike3.setApprovalStatus(ApprovalStatus.APPROVED);
        bike3.setOwner(owner);
        bike3.setEngineCapacity(125);
        bike3.setLocation(locDanang);
        vehicleRepository.save(bike3);


        // 4. TẠO DỮ LIỆU BOOKING
        Booking booking = new Booking();
        booking.setBookingDate(new Date());
        booking.setStartDate(java.sql.Date.valueOf(LocalDate.of(2026, 7, 1)));
        booking.setEndDate(java.sql.Date.valueOf(LocalDate.of(2026, 7, 5)));
        booking.setTotalAmount(2800000.0);
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setRenter(renter);
        booking.setVehicle(car2); // Gán xe bận lịch vào đơn đặt
        bookingRepository.save(booking);


        // 5. TẠO DỮ LIỆU PAYMENT
        Payment payment = new Payment();
        payment.setAmount(1000000.0);
        payment.setPaymentDate(new Date());
        payment.setPaymentType(PaymentType.DEPOSIT);
        payment.setBooking(booking);
        paymentRepository.save(payment);

        System.out.println("=== KHỞI TẠO DỮ LIỆU MẪU THÀNH CÔNG ===");
    }
}