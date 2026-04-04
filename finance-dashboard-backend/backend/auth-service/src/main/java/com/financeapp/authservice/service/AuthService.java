package com.financeapp.authservice.service;

import com.financeapp.authservice.dto.AuthDtos;
import com.financeapp.authservice.exception.AuthException;
import com.financeapp.authservice.model.User;
import com.financeapp.authservice.repository.UserRepository;
import com.financeapp.authservice.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + request.getEmail());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : User.Role.VIEWER);
        user.setStatus(User.Status.ACTIVE);

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved);

        return new AuthDtos.AuthResponse(
                token,
                saved.getEmail(),
                saved.getName(),
                saved.getRole().name(),
                saved.getStatus().name(),
                jwtUtil.getExpirationMs()
        );
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException("Invalid email or password");
        }

        if (user.getStatus() == User.Status.INACTIVE) {
            throw new AuthException("Account is inactive. Please contact an administrator.");
        }

        String token = jwtUtil.generateToken(user);

        return new AuthDtos.AuthResponse(
                token,
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                user.getStatus().name(),
                jwtUtil.getExpirationMs()
        );
    }

    public AuthDtos.UserResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("User not found"));
        return new AuthDtos.UserResponse(user);
    }
}
