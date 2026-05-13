package com.example.product.repository;

import com.example.product.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.saolasoft.base.persistence.repository.VoidableRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends VoidableRepository<User, Long> {

    Optional<User> findByUsernameAndVoidedFalse(String username);

    boolean existsByUsernameAndVoidedFalse(String username);

    // Override các method dùng generic ID (Serializable) vì Hibernate 6.3.1 không thể
    // so sánh 'Long' với 'Serializable' trong type check — fix bằng @Query cụ thể.
    @Override
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.voided = false")
    User findByIdAndVoidedFalse(@Param("id") Long id);

    @Override
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.id = :id AND u.voided = false")
    boolean existsByIdAndVoidedFalse(@Param("id") Long id);

    @Override
    @Query("SELECT u FROM User u WHERE u.voided = false")
    List<User> findAllByVoidedFalse();
}
