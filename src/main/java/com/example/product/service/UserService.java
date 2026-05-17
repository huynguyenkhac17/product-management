package com.example.product.service;

import com.example.product.dto.RegisterRequest;
import com.example.product.dto.UserDtoGet;
import com.example.product.entity.User;
import vn.saolasoft.base.service.VoidableDtoService;

public interface UserService extends VoidableDtoService<UserDtoGet, User, Long> {
    // VoidableDtoService có sẵn CRUD cơ bản

    UserDtoGet register(RegisterRequest req);

    User authenticate(String username, String rawPassword);
}
