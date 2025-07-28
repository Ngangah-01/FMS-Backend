package com.example.fleetmanagementsystem.controller;

import com.example.fleetmanagementsystem.DTO.ApiResponse;
import com.example.fleetmanagementsystem.DTO.ChangePasswordDTO;
import com.example.fleetmanagementsystem.model.Users;
import com.example.fleetmanagementsystem.services.EmailService;
import com.example.fleetmanagementsystem.services.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserController(UserService userService, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        log.info("Password change request for user");

        // Get authenticated user's ID from SecurityContext
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Users> userOptional = userService.findByEmail(username); // Assuming email is the username
        if (userOptional.isEmpty()) {
            log.warn("Authenticated user not found: {}", username);
            return new ResponseEntity<>(new ApiResponse<>(0,  "User not found"), HttpStatus.NOT_FOUND);
        }

        Users user = userOptional.get();

        // Verify current password
        if (!passwordEncoder.matches(changePasswordDTO.getCurrentPassword(), user.getPassword())) {
            log.warn("Invalid current password for user: {}", username);
            return new ResponseEntity<>(new ApiResponse<>(0,  "Current password is incorrect"), HttpStatus.BAD_REQUEST);
        }

        // Verify new password matches confirmation
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            log.warn("New password and confirmation do not match for user: {}", username);
            return new ResponseEntity<>(new ApiResponse<>(0,  "New password and confirmation do not match"), HttpStatus.BAD_REQUEST);
        }

        // Update password
        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userService.saveUser(user);
        log.info("Password updated successfully for user: {}", username);

        // Send email notification
        try {
            String role = user.getRoles().stream()
                    .filter(r -> !r.startsWith("ROLE_"))
                    .findFirst()
                    .orElse("USER");
            emailService.sendPasswordChangeEmail(user.getEmail(), user.getIdNumber(), role);
        } catch (Exception e) {
            log.error("Failed to send password change email for user {}: {}", username, e.getMessage());
        }

        return new ResponseEntity<>(new ApiResponse<>(1,"Password changed successfully"), HttpStatus.OK);
    }
}