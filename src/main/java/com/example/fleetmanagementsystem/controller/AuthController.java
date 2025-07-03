package com.example.fleetmanagementsystem.controller;

import com.example.fleetmanagementsystem.DTO.ApiResponse;
import com.example.fleetmanagementsystem.model.Users;
import com.example.fleetmanagementsystem.repositories.UserRepository;
import com.example.fleetmanagementsystem.config.JwtUtil;
import com.example.fleetmanagementsystem.services.EmailService;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Endpoint for admin registration.
     * Only allows users with ADMIN role to register.
     */

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Restrict registration to ADMIN role
        if (!"ADMIN".equalsIgnoreCase(request.getRole())) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse(0, "Only admins can register via this endpoint"));
        }

        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse(0, "Username already exists"));
        }

        // Create and save user 
        Users user = new Users();
        user.setUsername(request.getUsername());
        String plainPassword = request.getPassword(); // Store plain password for email
        user.setPassword(passwordEncoder.encode(plainPassword));
        user.setEmail(request.getEmail());
        Set<String> roles = new HashSet<>();
        roles.add(request.getRole().toUpperCase());
        user.setRoles(roles);
        Users savedUser = userRepository.save(user);

        // Prepare response data
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("id", savedUser.getId());
        responseData.put("username", savedUser.getUsername());
        responseData.put("Email", savedUser.getEmail());
        responseData.put("role", savedUser.getRoles().iterator().next());
        try {
            emailService.sendAccountCreationEmail(
                    savedUser.getEmail(),
                    savedUser.getUsername(),
                    plainPassword // Send plain password for email
                    , savedUser.getRoles().iterator().next() // Send role for email
            );
        } catch (Exception e) {
            // Log error but don't fail the request
            System.err.println("Failed to send account creation email: " + e.getMessage());
        }

        // Return success response
        return ResponseEntity.ok(
                new ApiResponse(1, "Admin registered successfully", responseData));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            String jwt = jwtUtil.generateToken(
                    request.getUsername(),
                    authentication.getAuthorities().stream()
                            .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                            .collect(Collectors.toSet()));

            Map<String, Object> data = new HashMap<>();
            data.put("token", jwt);
            data.put("username", request.getUsername());
            data.put("roles", authentication.getAuthorities().stream()
                    .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                    .collect(Collectors.toList()));

            return ResponseEntity.ok(
                    new ApiResponse<>(1, "Login successful", data));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>(0, "Invalid username or password", null));
        }
    }

    public static class RegisterRequest {
        @Getter
        @NotBlank
        private String username;
        @NotBlank
        private String password;
        @NotBlank
        private String email;
        @NotBlank
        private String role;

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    public static class LoginRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class JwtResponse {
        private final String token;

        public JwtResponse(String token, String loginSuccessful) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }
    }
}