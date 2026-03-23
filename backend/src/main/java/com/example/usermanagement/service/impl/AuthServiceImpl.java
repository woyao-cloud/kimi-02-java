package com.example.usermanagement.service.impl;

import com.example.usermanagement.dto.AuthDTO;
import com.example.usermanagement.entity.LoginAttempt;
import com.example.usermanagement.entity.Role;
import com.example.usermanagement.entity.User;
import com.example.usermanagement.entity.UserSession;
import com.example.usermanagement.exception.AuthenticationException;
import com.example.usermanagement.exception.BusinessException;
import com.example.usermanagement.repository.LoginAttemptRepository;
import com.example.usermanagement.repository.RoleRepository;
import com.example.usermanagement.repository.UserRepository;
import com.example.usermanagement.repository.UserSessionRepository;
import com.example.usermanagement.security.JwtTokenProvider;
import com.example.usermanagement.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserSessionRepository userSessionRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${security.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${security.lockout-duration-minutes:30}")
    private int lockoutDurationMinutes;

    @Override
    @Transactional
    public AuthDTO.LoginResponse login(AuthDTO.LoginRequest request, HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);

        // Check for brute force
        if (isAccountLocked(request.getUsernameOrEmail(), ipAddress)) {
            recordLoginAttempt(request.getUsernameOrEmail(), ipAddress, LoginAttempt.AttemptStatus.LOCKED,
                    "Account temporarily locked due to too many failed attempts");
            throw new AuthenticationException("ACCOUNT_LOCKED",
                    "Account temporarily locked. Please try again later.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()));

            User user = userRepository.findByUsernameOrEmail(
                            request.getUsernameOrEmail(), request.getUsernameOrEmail())
                    .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

            // Update last login
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            // Record successful login
            recordLoginAttempt(request.getUsernameOrEmail(), ipAddress,
                    LoginAttempt.AttemptStatus.SUCCESS, null);

            // Create session
            String accessToken = tokenProvider.generateAccessToken(
                    user.getId(), user.getUsername(), user.getEmail());
            String refreshToken = tokenProvider.generateRefreshToken(user.getId());

            createUserSession(user, accessToken, refreshToken, ipAddress,
                    httpRequest.getHeader("User-Agent"));

            return buildLoginResponse(user, accessToken, refreshToken);

        } catch (BadCredentialsException e) {
            recordLoginAttempt(request.getUsernameOrEmail(), ipAddress,
                    LoginAttempt.AttemptStatus.FAILED, "Invalid credentials");
            throw new AuthenticationException("INVALID_CREDENTIALS",
                    "Invalid username or password");
        }
    }

    @Override
    @Transactional
    public AuthDTO.LoginResponse register(AuthDTO.RegisterRequest request, HttpServletRequest httpRequest) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("USERNAME_EXISTS", "Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("EMAIL_EXISTS", "Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .active(true)
                .verified(false)
                .build();

        // Assign default role
        roleRepository.findByDefaultRoleTrue().ifPresent(user::addRole);

        User savedUser = userRepository.save(user);

        String ipAddress = getClientIpAddress(httpRequest);
        String accessToken = tokenProvider.generateAccessToken(
                savedUser.getId(), savedUser.getUsername(), savedUser.getEmail());
        String refreshToken = tokenProvider.generateRefreshToken(savedUser.getId());

        createUserSession(savedUser, accessToken, refreshToken, ipAddress,
                httpRequest.getHeader("User-Agent"));

        return buildLoginResponse(savedUser, accessToken, refreshToken);
    }

    @Override
    @Transactional
    public AuthDTO.TokenResponse refreshToken(AuthDTO.RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!tokenProvider.validateToken(refreshToken) ||
                !tokenProvider.isRefreshToken(refreshToken)) {
            throw new AuthenticationException("INVALID_REFRESH_TOKEN", "Invalid refresh token");
        }

        UserSession session = userSessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new AuthenticationException("INVALID_REFRESH_TOKEN",
                        "Session not found"));

        if (!session.isActive() || session.isExpired()) {
            throw new AuthenticationException("SESSION_EXPIRED", "Session has expired");
        }

        UUID userId = tokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("USER_NOT_FOUND", "User not found"));

        String newAccessToken = tokenProvider.generateAccessToken(
                user.getId(), user.getUsername(), user.getEmail());
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getId());

        // Update session
        session.setToken(newAccessToken);
        session.setRefreshToken(newRefreshToken);
        session.setExpiresAt(LocalDateTime.now().plusSeconds(
                tokenProvider.getAccessTokenExpiration() / 1000));
        userSessionRepository.save(session);

        return AuthDTO.TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenExpiration() / 1000)
                .build();
    }

    @Override
    @Transactional
    public void logout(String token) {
        userSessionRepository.deactivateByToken(token);
    }

    @Override
    @Transactional
    public void logoutAllDevices(String userId) {
        userSessionRepository.deactivateAllByUserId(UUID.fromString(userId));
    }

    private void createUserSession(User user, String token, String refreshToken,
                                   String ipAddress, String userAgent) {
        UserSession session = UserSession.builder()
                .user(user)
                .token(token)
                .refreshToken(refreshToken)
                .expiresAt(LocalDateTime.now().plusSeconds(
                        tokenProvider.getAccessTokenExpiration() / 1000))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .active(true)
                .build();
        userSessionRepository.save(session);
    }

    private AuthDTO.LoginResponse buildLoginResponse(User user, String accessToken, String refreshToken) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(com.example.usermanagement.entity.Permission::getName)
                .collect(Collectors.toSet());

        return AuthDTO.LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenExpiration() / 1000)
                .user(AuthDTO.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .roles(roles)
                        .permissions(permissions)
                        .build())
                .build();
    }

    private boolean isAccountLocked(String usernameOrEmail, String ipAddress) {
        LocalDateTime since = LocalDateTime.now().minus(lockoutDurationMinutes, ChronoUnit.MINUTES);

        long failedAttempts = loginAttemptRepository.countFailedAttemptsByUsernameSince(
                usernameOrEmail, since);

        return failedAttempts >= maxLoginAttempts;
    }

    private void recordLoginAttempt(String username, String ipAddress,
                                    LoginAttempt.AttemptStatus status, String failureReason) {
        LoginAttempt attempt = LoginAttempt.builder()
                .username(username)
                .ipAddress(ipAddress)
                .attemptStatus(status)
                .failureReason(failureReason)
                .build();
        loginAttemptRepository.save(attempt);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
