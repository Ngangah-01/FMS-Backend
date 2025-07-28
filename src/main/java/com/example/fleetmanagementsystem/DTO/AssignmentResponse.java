package com.example.fleetmanagementsystem.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AssignmentResponse {
    private DriverInfo driver;
    private VehicleInfo vehicle;
    private LocalDateTime assignedAt;
    private String assignedBy;

    @Data
    @AllArgsConstructor
    public static class DriverInfo {
        private String firstname;
        private String lastname;
        private String contact;
        private String licenseNumber;
        private String email;
    }

    @Data
    @AllArgsConstructor
    public static class VehicleInfo {
        private int capacity;
        private String model;
        private String status;
        private String route;
    }
}
