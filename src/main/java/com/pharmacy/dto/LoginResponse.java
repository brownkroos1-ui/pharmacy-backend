package com.pharmacy.dto;

public class LoginResponse {
    private String token;
    private String role;
    private String refreshToken;

    public LoginResponse(String token, String role) {
        this.token = token;
        this.role = role;
    }

    public LoginResponse(String token, String role, String refreshToken) {
        this.token = token;
        this.role = role;
        this.refreshToken = refreshToken;
    }

    public String getToken() {
        return token;
    }

    public String getRole() {
        return role;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
} 
