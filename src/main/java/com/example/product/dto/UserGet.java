package com.example.product.dto;

import com.example.product.entity.User;
import vn.saolasoft.base.service.dto.DtoGet;

import java.util.Date;

public class UserGet extends DtoGet<User, Long> {

    private String username;
    private String fullName;
    private Boolean enabled;
    private Date dateCreated;

    public UserGet() {}

    public UserGet(User u) { super(u); }

    @Override
    public void parse(User u) {
        this.username = u.getUsername();
        this.fullName = u.getFullName();
        this.enabled = u.getEnabled();
        this.dateCreated = u.getDateCreated();
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Date getDateCreated() { return dateCreated; }
    public void setDateCreated(Date dateCreated) { this.dateCreated = dateCreated; }
}
