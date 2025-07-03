package com.example.fleetmanagementsystem.repositories;

import com.example.fleetmanagementsystem.model.Driver;
import com.example.fleetmanagementsystem.model.DriverVehicleAssignment;
import com.example.fleetmanagementsystem.model.Matatu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverVehicleAssignmentRepository extends JpaRepository<DriverVehicleAssignment, Long> {
    Optional<DriverVehicleAssignment> findByDriverAndUnassignedAtIsNull(Driver driver);
    Optional<DriverVehicleAssignment> findByMatatuAndUnassignedAtIsNull(Matatu matatu);
}