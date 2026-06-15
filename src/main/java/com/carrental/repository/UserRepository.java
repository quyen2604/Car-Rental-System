package com.carrental.repository;
import com.carrental.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    // Tìm kiếm user bằng email phục vụ cho việc Đăng nhập / Đăng ký
    Optional<User> findByEmail(String email);
}
