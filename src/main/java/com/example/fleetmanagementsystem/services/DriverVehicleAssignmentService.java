package com.example.fleetmanagementsystem.services;

import com.example.fleetmanagementsystem.DTO.ApiResponse;
import com.example.fleetmanagementsystem.DTO.AssignmentResponse;
import com.example.fleetmanagementsystem.model.Driver;
import com.example.fleetmanagementsystem.model.DriverVehicleAssignment;
import com.example.fleetmanagementsystem.model.Matatu;
import com.example.fleetmanagementsystem.repositories.DriverRepository;
import com.example.fleetmanagementsystem.repositories.DriverVehicleAssignmentRepository;
import com.example.fleetmanagementsystem.repositories.MatatuRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public ApiResponse assignVehicle(Long driverId, String plateNumber) {

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found with ID: " + driverId));

        Matatu matatu = matatuRepository.findById(plateNumber)
                .orElseThrow(() -> new RuntimeException("Matatu not found"));

        // Check if the driver already has an active assignment
        boolean hasActiveAssignment = assignmentRepository.existsByDriverDriverId(driverId);
        boolean isVehicleAssigned = assignmentRepository.existsByMatatuPlateNumber(plateNumber);

        if (hasActiveAssignment) {
            return new ApiResponse(0, "Driver already has an active vehicle assignment");
        }

        if (isVehicleAssigned) {
            return new ApiResponse(0, "Vehicle is already assigned to another driver");
        }

        // Create a new assignment since both are available
        DriverVehicleAssignment assignment = new DriverVehicleAssignment();
        assignment.setDriver(driver);
        assignment.setMatatu(matatu);
        assignment.setAssignedAt(LocalDateTime.now());

        String assignedBy = SecurityContextHolder.getContext().getAuthentication().getName();
        assignment.setAssignedBy(assignedBy); // Make sure this field exists in your entity

        assignmentRepository.save(assignment);

        // Map to response DTO
        AssignmentResponse.DriverInfo driverInfo = new AssignmentResponse.DriverInfo(
                driver.getFirstname(),
                driver.getLastname(),
                driver.getUser().getPhoneNumber(),
                driver.getLicenseNumber(),
                driver.getEmail()
        );

        AssignmentResponse.VehicleInfo vehicleInfo = new AssignmentResponse.VehicleInfo(
                matatu.getCapacity(),
                matatu.getModel(),
                matatu.getStatus(),
                matatu.getRoute()
        );

        AssignmentResponse responseDTO = new AssignmentResponse(driverInfo, vehicleInfo, assignment.getAssignedAt(), assignment.getAssignedBy());

        return new ApiResponse(1, "Vehicle assigned to driver successfully", responseDTO);
    }


    @Transactional
    public DriverVehicleAssignment saveAssignment(DriverVehicleAssignment assignment) {
        if (assignment.getDriver() == null || assignment.getMatatu() == null) {
            throw new IllegalArgumentException("Driver and Matatu must not be null");
        }
        return assignmentRepository.save(assignment);
    }

    public List<Matatu> getVehicleByPlateNumber(String plateNumber) {
        if (plateNumber == null || plateNumber.isEmpty()) {
            throw new IllegalArgumentException("Plate number must not be null or empty");
        }
        return matatuRepository.findByPlateNumber(plateNumber.toUpperCase());
    }

    // Delete assignment method
    @Transactional
    public ApiResponse<Void> deleteAssignment(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found with ID: " + driverId));

       boolean hasActiveAssignment = assignmentRepository.existsByDriverDriverId(driverId);
        if (!hasActiveAssignment) {
            return new ApiResponse(0,"Error", "Driver does not have an active vehicle assignment");
        }

        DriverVehicleAssignment assignment = assignmentRepository.findByDriverDriverId(driverId)
                .orElseThrow(() -> new EntityNotFoundException("No active assignment found for driver with ID: " + driverId));

        assignmentRepository.delete(assignment);

        return new ApiResponse(1, "Vehicle assignment deleted successfully");
    }

    // Update assignment method
    @Transactional
    public ApiResponse updateAssignment(Long driverId, String plateNumber) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found with ID: " + driverId));

        Matatu matatu = matatuRepository.findById(plateNumber)
                .orElseThrow(() -> new EntityNotFoundException("Matatu not found with plate number: " + plateNumber));

        // Check if a driver has an existing assignment and delete it
        assignmentRepository.findByDriverDriverId(driverId).ifPresent(assignmentRepository::delete);

        // Create and save new assignment
        DriverVehicleAssignment newAssignment = new DriverVehicleAssignment();
        newAssignment.setDriver(driver);
        newAssignment.setMatatu(matatu);
        newAssignment.setAssignedAt(LocalDateTime.now());

        assignmentRepository.save(newAssignment);

        return new ApiResponse(1, "Vehicle assignment updated successfully");
    }


    @Transactional(readOnly = true)
    public List<DriverVehicleAssignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Driver> getUnassignedDrivers() {
        List<Driver> allDrivers = driverRepository.findAll();
        List<Long> assignedDriverIds = assignmentRepository.findAll()
                .stream()
                .map(assignment -> assignment.getDriver().getDriverId())
                .toList();

        return allDrivers.stream()
                .filter(driver -> !assignedDriverIds.contains(driver.getDriverId()))
                .toList();
    }
}