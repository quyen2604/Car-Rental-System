package com.carrental.service;

import com.carrental.DTO.LoginRequest;
import com.carrental.model.entity.User;

public interface AuthService {
    User register(User user);
    User login(LoginRequest loginRequest);
    void logout(int userId);
}