package com.example.product.controller;

import com.example.product.dto.LoginRequest;
import com.example.product.dto.LoginResponse;
import com.example.product.dto.RegisterRequest;
import com.example.product.dto.UserGet;
import com.example.product.entity.User;
import com.example.product.security.JwtUtil;
import com.example.product.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.saolasoft.base.api.response.APIResponse;
import vn.saolasoft.base.api.response.APIResponseHeader;
import vn.saolasoft.base.api.response.APIResponseStatus;
import vn.saolasoft.base.exception.APIAuthenticationException;
import vn.saolasoft.base.exception.DuplicateIdentifierException;
import vn.saolasoft.base.exception.ObjectNotFoundException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final long expirationMs;

    public AuthController(UserService userService,
                          JwtUtil jwtUtil,
                          @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.expirationMs = expirationMs;
    }

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<APIResponse<UserGet>> register(@Valid @RequestBody RegisterRequest req) {
        try {
            UserGet created = userService.register(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(new APIResponse<>(
                    new APIResponseHeader(APIResponseStatus.CREATED, "User registered"),
                    created));
        } catch (DuplicateIdentifierException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new APIResponse<>(
                    new APIResponseHeader(APIResponseStatus.DUPLICATED, ex.getMessage()), null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new APIResponse<>(
                    new APIResponseHeader(APIResponseStatus.INTERNAL_SERVER_ERROR, ex.getMessage()), null));
        }
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<APIResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req) {
        try {
            User user = userService.authenticate(req.getUsername(), req.getPassword());
            String token = jwtUtil.generateToken(user.getId(), user.getUsername());
            LoginResponse body = new LoginResponse(token, user.getId(), user.getUsername(), expirationMs);
            return ResponseEntity.ok(new APIResponse<>(
                    new APIResponseHeader(APIResponseStatus.OK, "Login success"),
                    body));
        } catch (APIAuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new APIResponse<>(
                    new APIResponseHeader(APIResponseStatus.UNAUTHORIZED, ex.getMessage()), null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new APIResponse<>(
                    new APIResponseHeader(APIResponseStatus.INTERNAL_SERVER_ERROR, ex.getMessage()), null));
        }
    }

    // GET /api/auth/me
    @GetMapping("/me")
    public ResponseEntity<APIResponse<UserGet>> me() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !(auth.getPrincipal() instanceof Long userId)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new APIResponse<>(
                        new APIResponseHeader(APIResponseStatus.UNAUTHORIZED, "Not authenticated"), null));
            }
            UserGet user = userService.getById(userId);
            return ResponseEntity.ok(new APIResponse<>(
                    new APIResponseHeader(APIResponseStatus.FOUND, "User found"), user));
        } catch (ObjectNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new APIResponse<>(
                    new APIResponseHeader(APIResponseStatus.NOT_FOUND, ex.getMessage()), null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new APIResponse<>(
                    new APIResponseHeader(APIResponseStatus.INTERNAL_SERVER_ERROR, ex.getMessage()), null));
        }
    }
}
