package com.example.product.controller;

import com.example.product.dto.LoginRequest;
import com.example.product.dto.LoginResponse;
import com.example.product.dto.RegisterRequest;
import com.example.product.dto.UserDtoGet;
import com.example.product.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.saolasoft.base.api.response.APIResponse;
import vn.saolasoft.base.api.response.APIResponseHeader;
import vn.saolasoft.base.api.response.APIResponseStatus;
import vn.saolasoft.base.exception.APIAuthenticationException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<APIResponse<UserDtoGet>> register(@Valid @RequestBody RegisterRequest req) {
        UserDtoGet created = userService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(new APIResponse<>(
                new APIResponseHeader(APIResponseStatus.CREATED, "User registered"), created));
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<APIResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(new APIResponse<>(
                new APIResponseHeader(APIResponseStatus.OK, "Login success"),
                userService.login(req)));
    }

    // GET /api/auth/me
    @GetMapping("/me")
    public ResponseEntity<APIResponse<UserDtoGet>> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long userId)) {
            throw new APIAuthenticationException("Not authenticated");
        }
        return ResponseEntity.ok(new APIResponse<>(
                new APIResponseHeader(APIResponseStatus.FOUND, "User found"),
                userService.getById(userId)));
    }
}
