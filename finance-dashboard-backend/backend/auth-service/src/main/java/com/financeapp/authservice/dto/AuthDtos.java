package com.financeapp.authservice.dto;

import com.financeapp.authservice.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    // ── Register Request ─────────────────────────────────────────────────────

    public static class RegisterRequest {

        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        private String name;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        private User.Role role = User.Role.VIEWER;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public User.Role getRole() { return role; }
        public void setRole(User.Role role) { this.role = role; }
    }

    // ── Login Request ────────────────────────────────────────────────────────

    public static class LoginRequest {

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    // ── Auth Response ────────────────────────────────────────────────────────

    public static class AuthResponse {

        private String token;
        private String tokenType = "Bearer";
        private String email;
        private String name;
        private String role;
        private String status;
        private long expiresIn;

        public AuthResponse(String token, String email, String name, String role, String status, long expiresIn) {
            this.token = token;
            this.email = email;
            this.name = name;
            this.role = role;
            this.status = status;
            this.expiresIn = expiresIn;
        }

        public String getToken() { return token; }
        public String getTokenType() { return tokenType; }
        public String getEmail() { return email; }
        public String getName() { return name; }
        public String getRole() { return role; }
        public String getStatus() { return status; }
        public long getExpiresIn() { return expiresIn; }
    }

    // ── User Response (no password) ──────────────────────────────────────────

    public static class UserResponse {

        private Long id;
        private String name;
        private String email;
        private String role;
        private String status;
        private String createdAt;

        public UserResponse(User user) {
            this.id = user.getId();
            this.name = user.getName();
            this.email = user.getEmail();
            this.role = user.getRole().name();
            this.status = user.getStatus().name();
            this.createdAt = user.getCreatedAt() != null ? user.getCreatedAt().toString() : null;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getStatus() { return status; }
        public String getCreatedAt() { return createdAt; }
    }
}
