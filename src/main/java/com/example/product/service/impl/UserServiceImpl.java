package com.example.product.service.impl;

import com.example.product.dto.RegisterRequest;
import com.example.product.dto.UserGet;
import com.example.product.entity.User;
import com.example.product.repository.UserRepository;
import com.example.product.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.saolasoft.base.exception.APIAuthenticationException;
import vn.saolasoft.base.exception.DuplicateIdentifierException;
import vn.saolasoft.base.exception.ObjectNotFoundException;
import vn.saolasoft.base.util.AuditUtil;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserGet register(RegisterRequest req) {
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

        User saved = userRepository.save(user);
        return new UserGet(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public User authenticate(String username, String rawPassword) {
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

    @Override
    @Transactional(readOnly = true)
    public UserGet getById(Long id) {
        User user = userRepository.findByIdAndVoidedFalse(id);
        if (user == null) {
            throw new ObjectNotFoundException("User not found: " + id);
        }
        return new UserGet(user);
    }
}
