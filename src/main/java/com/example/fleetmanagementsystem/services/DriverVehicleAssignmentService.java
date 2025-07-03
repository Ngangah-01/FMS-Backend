package com.example.fleetmanagementsystem.services;

import com.example.fleetmanagementsystem.DTO.ApiResponse;
import com.example.fleetmanagementsystem.model.Driver;
import com.example.fleetmanagementsystem.model.DriverVehicleAssignment;
import com.example.fleetmanagementsystem.model.Matatu;
import com.example.fleetmanagementsystem.repositories.DriverRepository;
import com.example.fleetmanagementsystem.repositories.DriverVehicleAssignmentRepository;
import com.example.fleetmanagementsystem.repositories.MatatuRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DriverVehicleAssignmentService {
    private final DriverVehicleAssignmentRepository assignmentRepository;
    private final DriverRepository driverRepository;
    private final MatatuRepository matatuRepository;

    public DriverVehicleAssignmentService(DriverVehicleAssignmentRepository assignmentRepository,
                                          DriverRepository driverRepository,
                                          MatatuRepository matatuRepository) {
        this.assignmentRepository = assignmentRepository;
        this.driverRepository = driverRepository;
        this.matatuRepository = matatuRepository;
    }

    @Transactional
    public ApiResponse assignVehicle(Long driverId, String regNo) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found with ID: " + driverId));

        List<Matatu> matatus = matatuRepository.findByRegNo(regNo.toUpperCase());
        if (matatus.isEmpty()) {
            return new ApiResponse(0, "Vehicle not found with Reg No: " + regNo);
        }
        Matatu matatu = matatus.get(0); // Assuming regNo is unique

        // Check if driver already has a vehicle
        if (assignmentRepository.findByDriverAndUnassignedAtIsNull(driver).isPresent()) {
            return new ApiResponse(0, "Driver " + driver.getName() + " already has a vehicle assigned");
        }

        // Check if vehicle is already assigned
        if (assignmentRepository.findByMatatuAndUnassignedAtIsNull(matatu).isPresent()) {
            return new ApiResponse(0, "Vehicle with Reg No " + regNo + " is already assigned");
        }

        DriverVehicleAssignment assignment = new DriverVehicleAssignment();
        assignment.setDriver(driver);
        assignment.setMatatu(matatu);
        assignmentRepository.save(assignment);

        return new ApiResponse(1, "Vehicle assigned to driver successfully", assignment);
    }

    @Transactional
    public ApiResponse unassignVehicle(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found with ID: " + driverId));

        DriverVehicleAssignment assignment = assignmentRepository
                .findByDriverAndUnassignedAtIsNull(driver)
                .orElseThrow(() -> new EntityNotFoundException("No active vehicle assignment found for driver with ID: " + driverId));

        assignment.setUnassignedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        return new ApiResponse(1, "Vehicle unassigned from driver successfully");
    }

    public DriverVehicleAssignment saveAssignment(DriverVehicleAssignment assignment) {
        if (assignment.getDriver() == null || assignment.getMatatu() == null) {
            throw new IllegalArgumentException("Driver and Matatu must not be null");
        }
        return assignmentRepository.save(assignment);
    }

    public List<Matatu> getVehicleByRegNo(String regNo) {
        if (regNo == null || regNo.isEmpty()) {
            throw new IllegalArgumentException("Registration number must not be null or empty");
        }
        return matatuRepository.findByRegNo(regNo.toUpperCase());
    }
}