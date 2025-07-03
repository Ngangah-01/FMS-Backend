package com.example.fleetmanagementsystem.repositories;

import com.example.fleetmanagementsystem.model.Conductor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConductorRepository extends JpaRepository<Conductor, Long> {
//    Optional<Conductor> findByEmail(String email);
    // Additional query methods can be defined here if needed
}
