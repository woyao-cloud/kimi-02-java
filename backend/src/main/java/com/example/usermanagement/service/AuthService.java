package com.example.usermanagement.service;

import com.example.usermanagement.dto.AuthDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    AuthDTO.LoginResponse login(AuthDTO.LoginRequest request, HttpServletRequest httpRequest);

    AuthDTO.LoginResponse register(AuthDTO.RegisterRequest request, HttpServletRequest httpRequest);

    AuthDTO.TokenResponse refreshToken(AuthDTO.RefreshTokenRequest request);

    void logout(String token);

    void logoutAllDevices(String userId);
}
