package com.example.fleetmanagementsystem.controller;

import com.example.fleetmanagementsystem.DTO.ApiResponse;
import com.example.fleetmanagementsystem.model.*;
import com.example.fleetmanagementsystem.services.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final ConductorService conductorService;
    private final DriverService driverService;
    private final MarshallService marshallService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final DriverVehicleAssignmentService assignmentService;

    public AdminController(UserService userService,
            ConductorService conductorService,
            DriverService driverService,
            PasswordEncoder passwordEncoder,
            MarshallService marshallService, EmailService emailService,
            DriverVehicleAssignmentService assignmentService) {
        this.userService = userService;
        this.conductorService = conductorService;
        this.driverService = driverService;
        this.passwordEncoder = passwordEncoder;
        this.marshallService = marshallService;
        this.emailService = emailService;
        this.assignmentService = assignmentService;
    }

    @Data
    public static class UserDTO {
        @NotBlank(message = "Username is required")
        private String username;

        @NotBlank(message = "Password is required")
        private String password;

        @NotBlank(message = "Role is required")
        @Pattern(regexp = "DRIVER|MARSHALL|CONDUCTOR", message = "Role must be DRIVER, MARSHALL or CONDUCTOR")
        private String role;

        private String name; // For Conductor, Driver, Marshall

        @Email(message = "Invalid email format")
        private String email; // For Driver, Marshall

        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
        private String phoneNumber; // For Driver, Marshall

        private String licenseNumber; // For Driver

        private String stage; // For Marshall
    }

    @Data
    public static class AssignmentDTO {
        @NotNull(message = "Driver ID is required")
        private Long driverId;

        @NotBlank(message = "Plate No is required")
        private String plateNumber;

        public String getPlateNumber() {
            return plateNumber.toUpperCase();
        }

        public void setPlateNumber(String plateNumber) {
            this.plateNumber = plateNumber.toUpperCase();
        }

        @Setter
        @Getter
        public static class UnassignmentDTO {
            @NotNull(message = "Driver ID is required")
            private Long driverId;

        }
    }


    @PreAuthorize("hasAnyRole('MARSHALL', 'ADMIN')")
    @PostMapping("/assign-driver")
    public ResponseEntity<ApiResponse> assignDriverToVehicle(@Valid @RequestBody AssignmentDTO assignmentDTO) {
        ApiResponse response = assignmentService.assignVehicle(assignmentDTO.getDriverId(), assignmentDTO.getPlateNumber());
        return ResponseEntity.status(response.getStatus() == 1 ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @PreAuthorize("hasAnyRole('MARSHALL', 'ADMIN')")
    @PostMapping("/unassign-driver")
    public ResponseEntity<ApiResponse> unassignDriverFromVehicle(
            @Valid @RequestBody AssignmentDTO.UnassignmentDTO unassignmentDTO) {
        ApiResponse response = assignmentService.unassignVehicle(unassignmentDTO.getDriverId());
        return ResponseEntity.status(response.getStatus() == 1 ? HttpStatus.OK : HttpStatus.NOT_FOUND)
                .body(response);
    }

    /**
     * User Management
     * This section handles user creation, retrieval, and deletion.
     */

    @Transactional
    @PostMapping("/users")
    public ResponseEntity<ApiResponse> createUser(@Valid @RequestBody UserDTO userDTO) {
        // convert role to uppercase for consistency
        userDTO.setRole(userDTO.getRole().toUpperCase());

        // Check if the username exists
        if (userService.findByUsername(userDTO.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse(0, "Username already exists"));
        }
        if (!userDTO.getRole().equals("ADMIN")
                && (userDTO.getPhoneNumber() == null || userDTO.getPhoneNumber().isBlank())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(0, "Phone number is required for non-admin roles"));
        }

        // Validate role-specific fields
        String role = userDTO.getRole();
        switch (role) {
            case "DRIVER":
                if (userDTO.getLicenseNumber() == null || userDTO.getLicenseNumber().isBlank()) {
                    return ResponseEntity.badRequest().body(
                            new ApiResponse(0, "License number is required for DRIVER role"));
                }
                if (userDTO.getEmail() == null || userDTO.getEmail().isBlank()) {
                    return ResponseEntity.badRequest().body(
                            new ApiResponse(0, "Email is required for DRIVER role"));
                }
                break;
            case "MARSHALL":
                if (userDTO.getStage() == null || userDTO.getStage().isBlank()) {
                    return ResponseEntity.badRequest().body(
                            new ApiResponse(0, "Stage is required for MARSHALL role"));
                }
                if (userDTO.getEmail() == null || userDTO.getEmail().isBlank()) {
                    return ResponseEntity.badRequest().body(
                            new ApiResponse(0, "Email is required for MARSHALL role"));
                }
                break;
            case "CONDUCTOR":
                if (userDTO.getName() == null || userDTO.getName().isBlank()) {
                    return ResponseEntity.badRequest().body(
                            new ApiResponse(0, "Name is required for CONDUCTOR role"));
                }
                if (userDTO.getEmail() == null || userDTO.getEmail().isBlank()) {
                    return ResponseEntity.badRequest().body(
                            new ApiResponse(0, "Email is required for DRIVER role"));
                }
                break;

            default:
                return ResponseEntity.badRequest().body(
                        new ApiResponse(0, "Invalid role"));
        }

        // Create and save user
        Users user = new Users();
        user.setUsername(userDTO.getUsername());
        String plainPassword = userDTO.getPassword(); // Store plain password for email
        user.setPassword(passwordEncoder.encode(plainPassword));
        Set<String> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        user.setEmail(userDTO.getEmail());
        user.setName(userDTO.getName());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        Users savedUser = userService.saveUser(user);

        // Handle role-specific data
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("id", savedUser.getId());
        responseData.put("username", savedUser.getUsername());
        responseData.put("role", role);
        responseData.put("email", userDTO.getEmail());
        responseData.put("PhoneNumber", userDTO.getPhoneNumber());

        switch (role) {
            case "CONDUCTOR":
                Conductor conductor = new Conductor();
                conductor.setName(userDTO.getName());
                conductor.setEmail(userDTO.getEmail());
                conductor.setPhoneNumber(userDTO.getPhoneNumber());
                conductor.setUser(savedUser);
                conductorService.saveConductor(conductor);
                responseData.put("name", userDTO.getName());

                try {
                    emailService.sendAccountCreationEmail(
                            savedUser.getEmail(),
                            savedUser.getUsername(),
                            plainPassword,
                            role);
                } catch (Exception e) {
                    System.err.println("Failed to send account creation email: " + e.getMessage());
                }
                break;
            case "DRIVER":
                Driver driver = new Driver();
                driver.setName(userDTO.getName());
                driver.setEmail(userDTO.getEmail());
                driver.setPhoneNumber(userDTO.getPhoneNumber());
                driver.setLicenseNumber(userDTO.getLicenseNumber());
                driver.setUser(savedUser);
                driverService.saveDriver(driver);
                responseData.put("name", userDTO.getName());
                responseData.put("licenseNumber", userDTO.getLicenseNumber());
                try {
                    emailService.sendAccountCreationEmail(
                            savedUser.getEmail(),
                            savedUser.getUsername(),
                            // savedUser.getRoles().toString(),
                            plainPassword,
                            role);
                } catch (Exception e) {
                    // Log error but don't fail the request
                    System.err.println("Failed to send account creation email: " + e.getMessage());
                }

                break;
            case "MARSHALL":
                Marshall marshall = new Marshall();
                marshall.setName(userDTO.getName());
                marshall.setEmail(userDTO.getEmail());
                marshall.setPhoneNumber(userDTO.getPhoneNumber());
                marshall.setStage(userDTO.getStage());
                marshall.setUser(savedUser);
                marshallService.saveMarshallProfile(marshall);
                responseData.put("name", userDTO.getName());
                responseData.put("stage", userDTO.getStage());

                try {
                    emailService.sendAccountCreationEmail(
                            savedUser.getEmail(),
                            savedUser.getUsername(),
                            // savedUser.getRoles().toString(),
                            plainPassword,
                            role);
                } catch (Exception e) {
                    // Log error but don't fail the request
                    System.err.println("Failed to send account creation email: " + e.getMessage());
                }
                break;
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse(1, "User created successfully", responseData));
    }

    @GetMapping("/users")
    public ResponseEntity<List<Users>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // get all users with role ADMIN
    @GetMapping("/users/admins")
    public ResponseEntity<List<Users>> getAllAdmins() {
        List<Users> admins = userService.getAllUsers().stream()
                .filter(user -> user.getRoles().contains("ADMIN"))
                .toList();
        return ResponseEntity.ok(admins);
    }

    // get all users with role DRIVER or CONDUCTOR. Include ApiResponse format
    @GetMapping("/users/drivers")
    public ResponseEntity<ApiResponse> getAllDrivers() {
        List<Driver> drivers = driverService.getAllDrivers().stream()
                .filter(user -> user.getRoles().contains("DRIVER"))
                .toList();
        if (drivers.isEmpty()) {
            return ResponseEntity.status(404).body(
                    new ApiResponse(0, "No drivers found"));
        }
        return ResponseEntity
                .ok(new ApiResponse(1, "Drivers retrieved successfully", drivers));
    }

    // get all users with role MARSHALL include ApiResponse format
    @GetMapping("/users/marshalls")
    public ResponseEntity<ApiResponse> getAllMarshallProfiles() {
        List<Users> marshalls = userService.getAllUsers().stream()
                .filter(user -> user.getRoles().contains("MARSHALL"))
                .toList();
        if (marshalls.isEmpty()) {
            return ResponseEntity.status(404).body(
                    new ApiResponse(0, "No marshalls found"));
        }
        return ResponseEntity.ok(new ApiResponse(1, "Marshalls retrieved successfully", marshalls));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long id) {
        Optional<Users> userOptional = userService.getUserById(id);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body(
                    new ApiResponse(0, "User with id " + id + "not found"));
        }
        Users user = userOptional.get();
        String role = user.getRoles().stream().findFirst().orElse("");

        String email = user.getEmail();
        String username = user.getUsername();
        userService.deleteUser(id);

        try {
            emailService.sendAccountDeletionEmail(email, username, role);
        } catch (Exception e) {
            System.err.println("Failed to send account deletion email: " + e.getMessage());
        }

        return ResponseEntity.ok(
                new ApiResponse(1, "User with id " + id + " deleted successfully"));
    }

    /*
     * Trip Assignment
     * This section handles the assignment and unassignment of drivers to vehicles.
     */
}
