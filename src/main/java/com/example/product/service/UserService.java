package com.example.product.service;

import com.example.product.dto.RegisterRequest;
import com.example.product.dto.UserGet;
import com.example.product.entity.User;

public interface UserService {

    UserGet register(RegisterRequest req);

    User authenticate(String username, String rawPassword);

    UserGet getById(Long id);
}
