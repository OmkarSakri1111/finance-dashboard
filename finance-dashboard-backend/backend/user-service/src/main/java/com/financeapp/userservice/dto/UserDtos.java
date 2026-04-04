package com.financeapp.userservice.dto;

import com.financeapp.userservice.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserDtos {

    public static class CreateUserRequest {
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100)
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

    public static class UpdateUserRequest {
        @Size(min = 2, max = 100)
        private String name;

        @Email(message = "Invalid email format")
        private String email;

        private User.Role role;
        private User.Status status;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public User.Role getRole() { return role; }
        public void setRole(User.Role role) { this.role = role; }
        public User.Status getStatus() { return status; }
        public void setStatus(User.Status status) { this.status = status; }
    }

    public static class UserResponse {
        private Long id;
        private String name;
        private String email;
        private String role;
        private String status;
        private String createdAt;
        private String updatedAt;

        public UserResponse(User user) {
            this.id = user.getId();
            this.name = user.getName();
            this.email = user.getEmail();
            this.role = user.getRole().name();
            this.status = user.getStatus().name();
            this.createdAt = user.getCreatedAt() != null ? user.getCreatedAt().toString() : null;
            this.updatedAt = user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getStatus() { return status; }
        public String getCreatedAt() { return createdAt; }
        public String getUpdatedAt() { return updatedAt; }
    }
}
