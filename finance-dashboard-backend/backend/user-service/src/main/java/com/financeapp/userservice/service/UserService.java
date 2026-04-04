package com.financeapp.userservice.service;

import com.financeapp.userservice.dto.UserDtos;
import com.financeapp.userservice.exception.ResourceNotFoundException;
import com.financeapp.userservice.model.User;
import com.financeapp.userservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserDtos.UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserDtos.UserResponse::new)
                .collect(Collectors.toList());
    }

    public UserDtos.UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return new UserDtos.UserResponse(user);
    }

    public UserDtos.UserResponse createUser(UserDtos.CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + request.getEmail());
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : User.Role.VIEWER);
        user.setStatus(User.Status.ACTIVE);
        return new UserDtos.UserResponse(userRepository.save(user));
    }

    public UserDtos.UserResponse updateUser(Long id, UserDtos.UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!request.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already in use: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        return new UserDtos.UserResponse(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public Map<String, Object> getUserStats() {
        long total = userRepository.count();
        long active = userRepository.countByStatus(User.Status.ACTIVE);
        long inactive = userRepository.countByStatus(User.Status.INACTIVE);
        long admins = userRepository.countByRole(User.Role.ADMIN);
        long analysts = userRepository.countByRole(User.Role.ANALYST);
        long viewers = userRepository.countByRole(User.Role.VIEWER);

        return Map.of(
                "totalUsers", total,
                "activeUsers", active,
                "inactiveUsers", inactive,
                "byRole", Map.of(
                        "ADMIN", admins,
                        "ANALYST", analysts,
                        "VIEWER", viewers
                )
        );
    }
}
