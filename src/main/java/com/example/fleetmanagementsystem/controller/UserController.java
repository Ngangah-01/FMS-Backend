package com.example.fleetmanagementsystem.controller;

import com.example.fleetmanagementsystem.DTO.ApiResponse;
import com.example.fleetmanagementsystem.DTO.ChangePasswordDTO;
import com.example.fleetmanagementsystem.model.Users;
import com.example.fleetmanagementsystem.services.EmailService;
import com.example.fleetmanagementsystem.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
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
        logger.info("Password change request for user");

        // Get authenticated user's idNumber from SecurityContext
        String idNumberStr = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.debug("Authenticated idNumber: {}", idNumberStr);
        try {
            Long idNumber = Long.parseLong(idNumberStr);
            Optional<Users> userOptional = userService.findByIdNumber(idNumber);
            if (userOptional.isEmpty()) {
                logger.warn("User not found: {}", idNumberStr);
                return new ResponseEntity<>(new ApiResponse<>(0, "User not found"), HttpStatus.NOT_FOUND);
            }

            Users user = userOptional.get();

            // Verify current password
            if (!passwordEncoder.matches(changePasswordDTO.getCurrentPassword(), user.getPassword())) {
                logger.warn("Invalid current password for user: {}", idNumberStr);
                return new ResponseEntity<>(new ApiResponse<>(0, "Current password is incorrect"), HttpStatus.BAD_REQUEST);
            }

            // Verify new password matches confirmation
            if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
                logger.warn("New password and confirmation do not match for user: {}", idNumberStr);
                return new ResponseEntity<>(new ApiResponse<>(0, "New password and confirmation do not match"), HttpStatus.BAD_REQUEST);
            }

            // Update password
            user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
            userService.saveUser(user);
            logger.info("Password updated successfully for user: {}", idNumberStr);

            // Send email notification
            try {
                String role = user.getRoles().stream()
                        .filter(r -> !r.startsWith("ROLE_"))
                        .findFirst()
                        .orElse("USER");
                emailService.sendPasswordChangeEmail(user.getEmail(), user.getIdNumber(), role);
            } catch (Exception e) {
                logger.error("Failed to send password change email for user {}: {}", idNumberStr, e.getMessage());
            }

            return new ResponseEntity<>(new ApiResponse<>(1, "Password changed successfully", null), HttpStatus.OK);
        } catch (NumberFormatException e) {
            logger.error("Invalid idNumber format: {}", idNumberStr);
            return new ResponseEntity<>(new ApiResponse<>(0, "Invalid user ID format"), HttpStatus.BAD_REQUEST);
        }
    }
}

