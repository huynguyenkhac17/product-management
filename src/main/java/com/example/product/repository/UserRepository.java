package com.example.product.repository;

import com.example.product.entity.User;
import vn.saolasoft.base.persistence.repository.VoidableRepository;

import java.util.Optional;

public interface UserRepository extends VoidableRepository<User, Long> {

    Optional<User> findByUsernameAndVoidedFalse(String username);

    boolean existsByUsernameAndVoidedFalse(String username);
}
