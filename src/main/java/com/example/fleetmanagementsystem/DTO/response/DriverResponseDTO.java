package com.example.fleetmanagementsystem.DTO.response;

import com.example.fleetmanagementsystem.model.Driver;
import lombok.Data;

@Data
public class DriverResponseDTO {
    private Long driverId;
    private String firstname;
    private String lastname;
    private String licenseNumber;
    private String phoneNumber;
    private String email;

    public static DriverResponseDTO from(Driver driver) {
        DriverResponseDTO response = new DriverResponseDTO();
        response.setDriverId(driver.getDriverId());
        response.setFirstname(driver.getFirstname());
        response.setLastname(driver.getLastname());
        response.setLicenseNumber(driver.getLicenseNumber());
        response.setPhoneNumber(driver.getPhoneNumber());
        response.setEmail(driver.getEmail());
        return response;
    }
}
