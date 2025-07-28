package com.example.fleetmanagementsystem.controller;

import com.example.fleetmanagementsystem.DTO.ApiResponse;
import com.example.fleetmanagementsystem.DTO.UserResponse;
import com.example.fleetmanagementsystem.DTO.response.*;
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
import java.util.stream.Collectors;

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

    @Setter
    @Getter
    @Data
    public static class UserDTO {

        @NotNull(message = "ID Number is required")
        private Long idNumber;

        @NotBlank(message = "Password is required")
        private String password;

        @NotBlank(message = "Role is required")
        @Pattern(regexp = "DRIVER|MARSHALL|CONDUCTOR", message = "Role must be DRIVER, MARSHALL or CONDUCTOR")
        private String role;

        @NotBlank
        private String firstname; // For Conductor, Driver, Marshall

        @NotBlank
        private String lastname;

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

    // delete assignment
    @PreAuthorize("hasAnyRole('MARSHALL', 'ADMIN')")
    @DeleteMapping("/delete-assignment/{driverId}")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(@PathVariable Long driverId) {
        ApiResponse<Void> response = assignmentService.deleteAssignment(driverId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('MARSHALL', 'ADMIN')")
    @PutMapping("/update-assignment")
    public ResponseEntity<ApiResponse> updateDriverAssignment(@Valid @RequestBody AssignmentDTO assignmentDTO) {
        ApiResponse response = assignmentService.updateAssignment(assignmentDTO.getDriverId(), assignmentDTO.getPlateNumber());
        return ResponseEntity.status(response.getStatus() == 1 ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // viewing assignments
    @PreAuthorize("hasAnyRole('MARSHALL', 'ADMIN')")
    @GetMapping("/assignments")
    public ResponseEntity<ApiResponse> getAllAssignments() {
        List<DriverVehicleAssignment> assignments = assignmentService.getAllAssignments();
        if (assignments.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse(0, "No assignments found"));
        }
        List<Map<String, Object>> responseData = assignments.stream().map(assignment -> {
            Map<String, Object> data = new HashMap<>();
            data.put("driverId", assignment.getDriver().getDriverId());
            data.put("matatuPlate", assignment.getMatatu().getPlateNumber());
            data.put("assignedAt", assignment.getAssignedAt());
            data.put("assignedBy", assignment.getAssignedBy());
            return data;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse(1, "Assignments retrieved successfully", responseData));
    }

    // getting the list of unassigned drivers: who have no active vehicle assignment
    @PreAuthorize("hasAnyRole('MARSHALL', 'ADMIN')")
    @GetMapping("/unassigned-drivers")
    public ResponseEntity<ApiResponse> getUnassignedDrivers() {
        List<Driver> unassignedDrivers = assignmentService.getUnassignedDrivers();
        if (unassignedDrivers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse(0, "No unassigned drivers found"));
        }
        List<UserResponse> driverDTOs = unassignedDrivers.stream().map(driver -> {
            UserResponse dto = new UserResponse();
            dto.setIdNumber(driver.getDriverId());
            dto.setFirstname(driver.getFirstname());
            dto.setLastname(driver.getLastname());
            dto.setEmail(driver.getEmail());
            dto.setPhoneNumber(driver.getPhoneNumber());
            dto.setLicenseNumber(driver.getLicenseNumber());
            String role = driver.getUser().getRole().stream()
                    .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
                    .findFirst()
                    .orElse("DRIVER");
            dto.setEnabled(driver.getUser().isEnabled());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse(1, "Unassigned drivers retrieved successfully", driverDTOs));
    }

    /**
     * User Management
     * This section handles user creation, retrieval, and deletion.
     */

    @Transactional
    @PostMapping("/users")
    public ResponseEntity<ApiResponse> createUser(@Valid @RequestBody UserDTO userDTO) {
        // convert role to uppercase for consistency
        System.out.println("Received UserDTO: " + userDTO);
        userDTO.setRole(userDTO.getRole().toUpperCase());

        if (userDTO.getIdNumber() == null) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse(0, "ID number cannot be null")
            );
        }
        // Check if the username exists
        if (userService.findByidNumber(userDTO.getIdNumber()).isPresent()) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse(0, "ID Number already exists"));
        }
        if (!userDTO.getRole().equals("ADMIN")
                && (userDTO.getPhoneNumber() == null || userDTO.getPhoneNumber().isBlank())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(0, "Phone number is required for non-admin role"));
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
                if (userDTO.getLastname() == null || userDTO.getLastname().isBlank()) {
                    return ResponseEntity.badRequest().body(
                            new ApiResponse(0, "Name is required for CONDUCTOR role"));
                }
                if (userDTO.getFirstname() == null || userDTO.getFirstname().isBlank()) {
                    return ResponseEntity.badRequest().body(
                            new ApiResponse(0, "First name is required for CONDUCTOR role"));
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
        user.setIdNumber(userDTO.getIdNumber());
        String plainPassword = userDTO.getPassword(); // Store plain password for email
        user.setPassword(passwordEncoder.encode(plainPassword));
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_" + role); // Add "ROLE_" prefix to the role
        roles.add(role);
        user.setRoles(roles);
        user.setEmail(userDTO.getEmail());
        user.setFirstname(userDTO.getFirstname());
        user.setLastname(userDTO.getLastname());
        user.setPhoneNumber(userDTO.getPhoneNumber());
//        Users savedUser = userService.saveUser(user);

        // Handle role-specific data
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("Id_Number", user.getIdNumber());
        responseData.put("Role", role);
        responseData.put("email", userDTO.getEmail());
        responseData.put("PhoneNumber", userDTO.getPhoneNumber());

        switch (role) {
            case "CONDUCTOR":
                Conductor conductor = new Conductor();
                conductor.setConductorId(userDTO.getIdNumber());
                conductor.setFirstname(userDTO.getFirstname());
                conductor.setLastname(userDTO.getLastname());
                conductor.setEmail(userDTO.getEmail());
                conductor.setPhoneNumber(userDTO.getPhoneNumber());
                conductor.setUser(user);
                user.setConductor(conductor);
                responseData.put("Firstname", userDTO.getFirstname());
                responseData.put("Lastname", userDTO.getLastname());
                break;
            case "DRIVER":
                Driver driver = new Driver();
                driver.setDriverId(userDTO.getIdNumber());
                driver.setFirstname(userDTO.getFirstname());
                driver.setLastname(userDTO.getLastname());
                driver.setEmail(userDTO.getEmail());
                driver.setPhoneNumber(userDTO.getPhoneNumber());
                driver.setLicenseNumber(userDTO.getLicenseNumber());
                driver.setUser(user);
                user.setDriver(driver);
                responseData.put("Firstname", userDTO.getFirstname());
                responseData.put("Lastname", userDTO.getLastname());
                responseData.put("licenseNumber", userDTO.getLicenseNumber());
                break;
            case "MARSHALL":
                Marshall marshall = new Marshall();
                marshall.setMarshallId(userDTO.getIdNumber());
                marshall.setFirstname(userDTO.getFirstname());
                marshall.setLastname(userDTO.getLastname());
                marshall.setEmail(userDTO.getEmail());
                marshall.setPhoneNumber(userDTO.getPhoneNumber());
                marshall.setStage(userDTO.getStage());
                marshall.setUser(user);
                user.setMarshall(marshall); //setting the bidirectional relationship
//                savedUser.setMarshall(marshall);
//                marshallService.saveMarshallProfile(marshall);
                responseData.put("Firstname", userDTO.getFirstname());
                responseData.put("Lastname", userDTO.getLastname());
                responseData.put("stage", userDTO.getStage());
                break;
        }
        // Save the user and related entities
        Users savedUser = userService.saveUser(user);

        try {
            emailService.sendAccountCreationEmail(
                    savedUser.getEmail(),
                    savedUser.getIdNumber(),
                    // savedUser.getRole().toString(),
                    plainPassword,
                    role);
        } catch (Exception e) {
            // Log error but don't fail the request
            System.err.println("Failed to send account creation email: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse(1, "User created successfully", responseData));
    }

    @Transactional(readOnly = true)
    @GetMapping("/users")
    public ResponseEntity<ApiResponse> getAllUsers() {
        List<Users> users = userService.getAllUsers();
        if (users.isEmpty()) {
            return ResponseEntity.status(404).body(
                    new ApiResponse(0, "No users found"));
        }
        List<UserResponse> userDTOs = users.stream().map(user -> {
            UserResponse dto = new UserResponse();
            dto.setIdNumber(user.getIdNumber());
            dto.setFirstname(user.getFirstname());
            dto.setLastname(user.getLastname());
            dto.setEmail(user.getEmail());
            dto.setPhoneNumber(user.getPhoneNumber());
            String role = user.getRole().stream()
                    .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
                    .findFirst()
                    .orElse(null);
            dto.setRole(role);
            dto.setEnabled(user.isEnabled());
            if (user.getRole().contains("ROLE_MARSHALL") && user.getMarshall() != null) {
                dto.setStage(user.getMarshall().getStage());
            }
            if (user.getRole().contains("ROLE_DRIVER") && user.getDriver() != null) {
                dto.setLicenseNumber(user.getDriver().getLicenseNumber());
            }
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse(1, "Users retrieved successfully", userDTOs));
    }

    //get user by IDnumber
    @Transactional(readOnly = true)
    @GetMapping("/users/{idNumber}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long idNumber) {
        Optional<Users> userOptional = userService.getUserById(idNumber);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body(
                    new ApiResponse(0, "User with ID " + idNumber + " not found"));
        }
        Users user = userOptional.get();
        UserResponse dto = new UserResponse();
        dto.setIdNumber(user.getIdNumber());
        dto.setFirstname(user.getFirstname());
        dto.setLastname(user.getLastname());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        String role = user.getRole().stream()
                .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
                .findFirst()
                .orElse(null);
        dto.setRole(role);
        dto.setEnabled(user.isEnabled());
        if (user.getRole().contains("ROLE_MARSHALL") && user.getMarshall() != null) {
            dto.setStage(user.getMarshall().getStage());
        }
        if (user.getRole().contains("ROLE_DRIVER") && user.getDriver() != null) {
            dto.setLicenseNumber(user.getDriver().getLicenseNumber());
        }
        return ResponseEntity.ok(new ApiResponse(1, "User retrieved successfully", dto));
    }

    //updating any user details
    @Transactional
    @PutMapping("/users/{idNumber}")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable Long idNumber, @Valid @RequestBody UserDTO userDTO) {
        Optional<Users> userOptional = userService.getUserById(idNumber);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body(
                    new ApiResponse(0, "User with ID " + idNumber + " not found"));
        }
        Users user = userOptional.get();

        user.setFirstname(userDTO.getFirstname());
        user.setLastname(userDTO.getLastname());
        user.setEmail(userDTO.getEmail());
        user.setPhoneNumber(userDTO.getPhoneNumber());

        String role = userDTO.getRole().toUpperCase();

        if (role.equals("DRIVER")) {
            if (userDTO.getLicenseNumber() == null || userDTO.getLicenseNumber().isBlank()) {
                return ResponseEntity.badRequest().body(
                        new ApiResponse(0, "License number is required for DRIVER role"));
            }
            Driver driver = user.getDriver();
            if (driver == null) {
                driver = new Driver();
                driver.setDriverId(idNumber);
                driver.setUser(user);
                user.setDriver(driver);
            }

            driver.setDriverId(userDTO.getIdNumber());
            driver.setFirstname(userDTO.getFirstname());
            driver.setLastname(userDTO.getLastname());
            driver.setEmail(userDTO.getEmail());
            driver.setPhoneNumber(userDTO.getPhoneNumber());
            driver.setLicenseNumber(userDTO.getLicenseNumber());

            DriverResponseDTO responseDTO = DriverResponseDTO.from(driver);
            return ResponseEntity.ok(new ApiResponse(1, "Driver updated successfully", responseDTO));
        } else if (role.equals("MARSHALL")) {
            Marshall marshall = user.getMarshall();
            if (marshall == null) {
                marshall = new Marshall();
                marshall.setMarshallId(idNumber);
                marshall.setUser(user);
                user.setMarshall(marshall);
            }

            marshall.setMarshallId(userDTO.getIdNumber());
            marshall.setStage(userDTO.getStage());
            marshall.setFirstname(userDTO.getFirstname());
            marshall.setLastname(userDTO.getLastname());
            marshall.setEmail(userDTO.getEmail());
            marshall.setPhoneNumber(userDTO.getPhoneNumber());


            MarshallResponseDTO responseDTO = MarshallResponseDTO.from(user.getMarshall());

            return ResponseEntity.ok(new ApiResponse(1, "Marshall updated successfully", responseDTO));
        } else if (role.equals("CONDUCTOR")) {
            Conductor conductor = user.getConductor();
            if (conductor == null) {
                conductor = new Conductor();
                conductor.setConductorId(idNumber);
                conductor.setUser(user);
                user.setConductor(conductor);
            }

            conductor.setConductorId(userDTO.getIdNumber());
            conductor.setFirstname(userDTO.getFirstname());
            conductor.setLastname(userDTO.getLastname());
            conductor.setEmail(userDTO.getEmail());
            conductor.setPhoneNumber(userDTO.getPhoneNumber());

            ConductorResponseDTO responseDTO = ConductorResponseDTO.from(user.getConductor());

            return ResponseEntity.ok(new ApiResponse(1, "Conductor updated successfully", responseDTO));
        } else {
            return ResponseEntity.badRequest().body(
                    new ApiResponse(0, "Invalid role for update"));
        }
    }

   //getting all users who are admins
    @Transactional(readOnly = true)
    @GetMapping("/users/admins")
    public ResponseEntity<ApiResponse> getAllAdmins() {
        List<Users> admins = userService.getAllAdmins();
        if (admins.isEmpty()) {
            return ResponseEntity.status(404).body(
                    new ApiResponse(0, "No admins found"));
        }
        List<UserResponse> adminDTOs = admins.stream().map(admin -> {
            UserResponse dto = new UserResponse();
            dto.setIdNumber(admin.getIdNumber());
            dto.setFirstname(admin.getFirstname());
            dto.setLastname(admin.getLastname());
            dto.setEmail(admin.getEmail());
            dto.setPhoneNumber(admin.getPhoneNumber());
            String role = admin.getRole().stream()
                    .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
                    .findFirst()
                    .orElse("ADMIN");
            dto.setRole(role);
            dto.setEnabled(admin.isEnabled());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse(1, "Admins retrieved successfully", adminDTOs));
    }



    @Transactional(readOnly = true)
    @GetMapping("/users/drivers")
    public ResponseEntity<ApiResponse> getAllDrivers() {
        List<Driver> drivers = driverService.getAllDrivers();
        if (drivers.isEmpty()) {
            return ResponseEntity.status(404).body(
                    new ApiResponse(0, "No drivers found"));
        }
        List<UserResponse> driverDTOs = drivers.stream().map(driver -> {
            UserResponse dto = new UserResponse();
            dto.setIdNumber(driver.getDriverId());
            dto.setFirstname(driver.getFirstname());
            dto.setLastname(driver.getLastname());
            dto.setEmail(driver.getEmail());
            dto.setPhoneNumber(driver.getPhoneNumber());
            dto.setLicenseNumber(driver.getLicenseNumber());
            String role = driver.getUser().getRole().stream()
                    .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
                    .findFirst()
                    .orElse("DRIVER");
            dto.setRole(role);
            dto.setEnabled(driver.getUser().isEnabled());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse(1, "Drivers retrieved successfully", driverDTOs));
    }


    @Transactional(readOnly = true)
    @GetMapping("/users/marshalls")
    public ResponseEntity<ApiResponse> getAllMarshallProfiles() {
        List<Marshall> marshalls = marshallService.getAllMarshallProfiles();
        if (marshalls.isEmpty()) {
            return ResponseEntity.status(404).body(
                    new ApiResponse(0, "No marshalls found"));
        }
        List<UserResponse> marshallDTOs = marshalls.stream().map(marshall -> {
            UserResponse dto = new UserResponse();
            dto.setIdNumber(marshall.getMarshallId());
            dto.setFirstname(marshall.getFirstname());
            dto.setLastname(marshall.getLastname());
            dto.setEmail(marshall.getEmail());
            dto.setPhoneNumber(marshall.getPhoneNumber());
            dto.setStage(marshall.getStage());
            String role = marshall.getUser().getRole().stream()
                    .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
                    .findFirst()
                    .orElse("MARSHALL");
            dto.setRole(role);
            dto.setEnabled(marshall.getUser().isEnabled());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse(1, "Marshalls retrieved successfully", marshallDTOs));
    }

    @Transactional(readOnly = true)
    @GetMapping("/users/conductors")
    public ResponseEntity<ApiResponse> getAllConductors() {
        List<Conductor> conductors = conductorService.getAllConductors();
        if (conductors.isEmpty()) {
            return ResponseEntity.status(404).body(
                    new ApiResponse(0, "No conductors found"));
        }
        List<UserResponse> conductorDTOs = conductors.stream().map(conductor -> {
            UserResponse dto = new UserResponse();
            dto.setIdNumber(conductor.getConductorId());
            dto.setFirstname(conductor.getFirstname());
            dto.setLastname(conductor.getLastname());
            dto.setEmail(conductor.getEmail());
            dto.setPhoneNumber(conductor.getPhoneNumber());
            String role = conductor.getUser().getRole().stream()
                    .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
                    .findFirst()
                    .orElse("CONDUCTOR");
            dto.setRole(role);
            dto.setEnabled(conductor.getUser().isEnabled());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse(1, "Conductors retrieved successfully", conductorDTOs));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long idNumber) {
        Optional<Users> userOptional = userService.getUserById(idNumber);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body(
                    new ApiResponse(0, "User with id " + idNumber + "not found"));
        }
        Users user = userOptional.get();
        String role = user.getRole().stream().findFirst().orElse("");

        String email = user.getEmail();
        Long id = user.getIdNumber();
        // Delete the user
        userService.deleteUser(idNumber);

        try {
            emailService.sendAccountDeletionEmail(email, id, role);
        } catch (Exception e) {
            System.err.println("Failed to send account deletion email: " + e.getMessage());
        }

        return ResponseEntity.ok(
                new ApiResponse(1, "User with id " + id + " deleted successfully"));
    }

    //change user password for any user who is logged in
    @Transactional
    @PutMapping("/users/{idNumber}/change-password")
    public ResponseEntity<ApiResponse> changeUserPassword(@PathVariable Long idNumber,
                                                           @Valid @RequestBody UserDTO userDTO) {
        Optional<Users> userOptional = userService.getUserById(idNumber);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body(
                    new ApiResponse(0, "User with ID " + idNumber + " not found"));
        }
        Users user = userOptional.get();
        String newPassword = userDTO.getPassword();
        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse(0, "New password cannot be empty"));
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        Users updatedUser = userService.saveUser(user);

        return ResponseEntity.ok(new ApiResponse(1, "Password changed successfully", updatedUser));
    }




    /*
     * Trip Assignment
     * This section handles the assignment and unassignment of drivers to vehicles.
     */
}
