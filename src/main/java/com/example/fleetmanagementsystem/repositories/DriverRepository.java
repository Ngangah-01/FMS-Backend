package com.example.fleetmanagementsystem.repositories;

import com.example.fleetmanagementsystem.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Object> findById(Driver driverId);
}

