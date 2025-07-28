package com.example.fleetmanagementsystem.repositories;

import com.example.fleetmanagementsystem.model.Driver;
import com.example.fleetmanagementsystem.model.DriverVehicleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverVehicleAssignmentRepository extends JpaRepository<DriverVehicleAssignment, Long> {
//
    boolean existsByDriverDriverId(Long driverId);

    boolean existsByMatatuPlateNumber(String plateNumber);

    Optional<DriverVehicleAssignment> findByDriverDriverId(Long driverId);
    Optional<DriverVehicleAssignment> findByMatatuPlateNumber(String plateNumber);
}