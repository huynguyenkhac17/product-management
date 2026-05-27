package com.example.product.service.impl;

import com.example.product.dto.LoginRequest;
import com.example.product.dto.LoginResponse;
import com.example.product.dto.RegisterRequest;
import com.example.product.dto.UserDtoGet;
import com.example.product.entity.User;
import com.example.product.repository.UserRepository;
import com.example.product.security.JwtUtil;
import com.example.product.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.saolasoft.base.exception.APIAuthenticationException;
import vn.saolasoft.base.exception.DuplicateIdentifierException;
import vn.saolasoft.base.service.impl.VoidableDtoJpaServiceImpl;
import vn.saolasoft.base.util.AuditUtil;

import java.util.Set;

@Service
public class UserServiceImpl
        extends VoidableDtoJpaServiceImpl<UserDtoGet, User, Long>
        implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    @Override
    public UserRepository getRepository() {
        return userRepository;
    }

    @Override
    public UserDtoGet convert(User user) {
        return new UserDtoGet(user);
    }

    @Override
    public Set<String> getSortableColumns() {
        return Set.of("id", "username", "fullName", "dateCreated");
    }

    @Override
    public UserDtoGet register(RegisterRequest req) {
        if (userRepository.existsByUsernameAndVoidedFalse(req.getUsername())) {
            throw new DuplicateIdentifierException("Username already exists: " + req.getUsername());
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setFullName(req.getFullName());
        user.setEnabled(Boolean.TRUE);
        user.setVoided(Boolean.FALSE);

        AuditUtil.addCreationInformation(user, null);

        return convert(userRepository.save(user));
    }

    @Override
    public LoginResponse login(LoginRequest req) {
        User user = authenticate(req.getUsername(), req.getPassword());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new LoginResponse(token, user.getId(), user.getUsername(), expirationMs);
    }

    // chỉ login() dùng
    private User authenticate(String username, String rawPassword) {
        User user = userRepository.findByUsernameAndVoidedFalse(username)
                .orElseThrow(() -> new APIAuthenticationException("Invalid username or password"));

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new APIAuthenticationException("User is disabled");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new APIAuthenticationException("Invalid username or password");
        }
        return user;
    }
}
