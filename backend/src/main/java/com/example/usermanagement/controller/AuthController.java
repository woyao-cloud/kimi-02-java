package com.example.usermanagement.controller;

import com.example.usermanagement.dto.ApiResponse;
import com.example.usermanagement.dto.AuthDTO;
import com.example.usermanagement.security.JwtTokenProvider;
import com.example.usermanagement.security.UserPrincipal;
import com.example.usermanagement.service.AuthService;
import com.example.usermanagement.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ApiResponse<AuthDTO.LoginResponse> login(
            @Valid @RequestBody AuthDTO.LoginRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.success(authService.login(request, httpRequest));
    }

    @PostMapping("/register")
    public ApiResponse<AuthDTO.LoginResponse> register(
            @Valid @RequestBody AuthDTO.RegisterRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.success(authService.register(request, httpRequest));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthDTO.TokenResponse> refreshToken(
            @Valid @RequestBody AuthDTO.RefreshTokenRequest request) {
        return ApiResponse.success(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        authService.logout(token);
        return ApiResponse.success(null);
    }

    @PostMapping("/logout-all")
    public ApiResponse<Void> logoutAllDevices(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        authService.logoutAllDevices(userPrincipal.getId().toString());
        return ApiResponse.success(null);
    }

    @GetMapping("/me")
    public ApiResponse<AuthDTO.CurrentUserResponse> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ApiResponse.success(userService.getCurrentUser(userPrincipal.getId()));
    }
}
