package com.example.product.dto;

public class LoginResponse {

    private String token;
    private Long userId;
    private String username;
    private long expiresInMs;

    public LoginResponse() {}

    public LoginResponse(String token, Long userId, String username, long expiresInMs) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.expiresInMs = expiresInMs;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public long getExpiresInMs() { return expiresInMs; }
    public void setExpiresInMs(long expiresInMs) { this.expiresInMs = expiresInMs; }
}
