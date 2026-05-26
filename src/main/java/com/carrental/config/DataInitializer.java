package com.carrental.config;

import com.carrental.entity.*;
import com.carrental.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Tạo user admin mặc định
        if (!userRepository.existsByEmail("admin@carrental.vn")) {
            User admin = new User();
            admin.setEmail("admin@carrental.vn");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setFullName("Super Admin");
            admin.setStatus(UserStatus.ACTIVE);
            userRepository.save(admin);
            System.out.println("✅ Đã tạo tài khoản admin (admin@carrental.vn / admin123)");
        }

        // Tạo chủ xe mặc định để gán cho các xe
        if (!userRepository.existsByEmail("owner@carrental.vn")) {
            User owner = new User();
            owner.setEmail("owner@carrental.vn");
            owner.setPassword(passwordEncoder.encode("owner123"));
            owner.setRole(Role.OWNER);
            owner.setFullName("Chủ Xe 1");
            owner.setStatus(UserStatus.ACTIVE);
            userRepository.save(owner);
            System.out.println("✅ Đã tạo tài khoản owner (owner@carrental.vn / owner123)");
        }

        // Chỉ thêm data nếu DB trống
        if (vehicleRepository.count() == 0) {
            User owner = userRepository.findByEmail("owner@carrental.vn").orElseThrow();

            Car car1 = new Car();
            car1.setOwner(owner);
            car1.setVehicleType(VehicleType.CAR);
            car1.setBrand("Toyota");
            car1.setModel("Vios");
            car1.setLicensePlate("51A-12345");
            car1.setPricePerDay(500000.0);
            car1.setDepositAmount(2000000.0);
            car1.setAddress("Quận 1, TP.HCM");
            car1.setStatus(VehicleStatus.AVAILABLE);
            car1.setDescription("Xe sedan 5 chỗ, tiết kiệm nhiên liệu");
            car1.setSeatCount(5);
            car1.setTransmission("AUTOMATIC");
            vehicleRepository.save(car1);

            Car car2 = new Car();
            car2.setOwner(owner);
            car2.setVehicleType(VehicleType.CAR);
            car2.setBrand("Honda");
            car2.setModel("City");
            car2.setLicensePlate("29A-67890");
            car2.setPricePerDay(600000.0);
            car2.setDepositAmount(2000000.0);
            car2.setAddress("Cầu Giấy, Hà Nội");
            car2.setStatus(VehicleStatus.AVAILABLE);
            car2.setDescription("Xe gia đình rộng rãi");
            car2.setSeatCount(5);
            car2.setTransmission("AUTOMATIC");
            vehicleRepository.save(car2);

            Motorbike moto1 = new Motorbike();
            moto1.setOwner(owner);
            moto1.setVehicleType(VehicleType.MOTORBIKE);
            moto1.setBrand("Honda");
            moto1.setModel("Wave RSX");
            moto1.setLicensePlate("59U1-23456");
            moto1.setPricePerDay(100000.0);
            moto1.setDepositAmount(500000.0);
            moto1.setAddress("Quận 3, TP.HCM");
            moto1.setStatus(VehicleStatus.AVAILABLE);
            moto1.setDescription("Xe tiết kiệm xăng, phù hợp đi nội thành");
            moto1.setEngineCapacity(110);
            vehicleRepository.save(moto1);

            System.out.println("✅ Đã thêm 3 xe mẫu vào database!");
        }
    }
}
