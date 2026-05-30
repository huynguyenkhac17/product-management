package com.example.product.entity;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import vn.saolasoft.base.persistence.model.VoidableSerialIDEntry;

@Entity
@Table(name = "app_user", uniqueConstraints = @UniqueConstraint(name = "uk_app_user_username", columnNames = "username"))
public class User extends VoidableSerialIDEntry {

    // role dùng enum string lưu trong db
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20)
    private Role role = Role.USER; // Mặc định user đều role USER

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "full_name", length = 200)
    private String fullName;

    // Voided
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = Boolean.TRUE;

    public Role getRole() {return role; }
    public void setRole(Role role) {this.role = role; }

    public String getUsername() {return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
