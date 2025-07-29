package com.example.fleetmanagementsystem.controller;

import com.example.fleetmanagementsystem.DTO.ApiResponse;
import com.example.fleetmanagementsystem.model.Users;
import com.example.fleetmanagementsystem.repositories.UserRepository;
import com.example.fleetmanagementsystem.config.JwtUtil;
import com.example.fleetmanagementsystem.services.EmailService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
     * Only allows users with an ADMIN role to register.
     */

    @Transactional
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Restrict registration to ADMIN role
        if (!"ADMIN".equalsIgnoreCase(request.getRole())) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse(0, "Only admins can register via this endpoint"));
        }

        // Check if the id already exists
        if (userRepository.findByidNumber(request.getIdNumber()).isPresent()) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse(0, "User ID Number already exists!"));
        }

        // Create and save user 
        Users user = new Users();
        user.setIdNumber(request.getIdNumber());
        user.setFirstname(request.getFirstName());
        user.setLastname(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());
        String plainPassword = request.getPassword(); // Store plain password for email
        user.setPassword(passwordEncoder.encode(plainPassword));
        Set<String> roles = new HashSet<>();
        roles.add(request.getRole().toUpperCase());
        user.setRoles(roles);
        Users savedUser = userRepository.save(user);

        // Prepare response data
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("idNumber", savedUser.getIdNumber());
        responseData.put("Firstname", savedUser.getFirstname());
        responseData.put("Lastname", savedUser.getLastname());
        responseData.put("PhoneNumber", savedUser.getPhoneNumber());
        responseData.put("Email", savedUser.getEmail());
        responseData.put("role", savedUser.getRole().iterator().next());
        try {
            emailService.sendAccountCreationEmail(
                    savedUser.getEmail(),
                    savedUser.getIdNumber(),
                    plainPassword // Send plain password for email
                    ,savedUser.getRole().iterator().next() // Send a role for email
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
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @NotNull @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getIdNumber(), request.getPassword()));

            String role = authentication.getAuthorities().stream()
                    .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                    .findFirst()
                    .orElse("UNKNOWN");

            String jwt = jwtUtil.generateToken(
                    request.getIdNumber(),
                    Set.of(role));

            Map<String, Object> data = new HashMap<>();
            data.put("token", jwt);
            data.put("ID Number", request.getIdNumber());
            data.put("role", role); // ✅ Plain string now

            return ResponseEntity.ok(
                    new ApiResponse<>(1, "Login successful", data));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>(0, "Invalid ID Number or password", null));
        }
    }






    public static class RegisterRequest {
        @Setter
        @Getter
        @NotNull
        private Long idNumber;

        @Setter
        @Getter
        @NotBlank
        private String firstName;

        @Setter
        @Getter
        @NotBlank
        private String lastName;

        @Setter
        @Getter
        @NotBlank
        private String phoneNumber;

        @Email
        @Setter
        @Getter
        @NotBlank
        private String email;

        @Setter
        @Getter
        @NotBlank
        private String password;

        @Setter
        @Getter
        @NotBlank
        private String role;

    }


    public static class LoginRequest {

        @Setter
        @Getter
        @NotNull
        private Long idNumber;

        @Setter
        @Getter
        @NotBlank
        private String password;

    }

    @Getter
    public static class JwtResponse {
        private final String token;

        public JwtResponse(String token, String loginSuccessful) {
            this.token = token;
        }

    }
}