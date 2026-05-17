package com.example.product.service;

import com.example.product.dto.RegisterRequest;
import com.example.product.dto.UserDTOGet;
import com.example.product.entity.User;
import vn.saolasoft.base.service.VoidableDtoService;

public interface UserService extends VoidableDtoService<UserDTOGet, User, Long> {
    // VoidableDtoService có sẵn CRUD cơ bản

    UserDTOGet register(RegisterRequest req);

    User authenticate(String username, String rawPassword);
}
