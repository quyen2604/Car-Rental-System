package com.carrental.config;

import com.carrental.model.entity.*;
import com.carrental.model.enums.*;
import com.carrental.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VehicleRepository vehicleRepository;

    @Override
    public void run(String... args) throws Exception {
        // 1. Kiểm tra nếu chưa có dữ liệu thì mới khởi tạo tránh trùng lặp
        if (userRepository.count() == 0) {

            // Tạo tài khoản Renter (Người thuê) mẫu
            Renter renter = new Renter();
            renter.setFullName("Nguyễn Văn A");
            renter.setEmail("renter@gmail.com");
            renter.setPhone("0912345678");
            renter.setPassword("123456"); // Tạm thời dùng pass thô để test nhanh
            renter.setActive(true);
            renter.setLicenseNumber("G1-12345");
            userRepository.save(renter);

            // Tạo tài khoản Owner (Chủ xe) mẫu
            Owner owner = new Owner();
            owner.setFullName("Trần Thị B");
            owner.setEmail("owner@gmail.com");
            owner.setPhone("0987654321");
            owner.setPassword("123456");
            owner.setActive(true);
            userRepository.save(owner);

            System.out.println(">> ĐÃ KHỞI TẠO DỮ LIỆU USER MẪU THÀNH CÔNG! <<");
            System.out.println("Tài khoản renter: renter@gmail.com / 123456");
            System.out.println("Tài khoản owner: owner@gmail.com / 123456");
        }
    }
}
