package com.example.fleetmanagementsystem.repositories;

import com.example.fleetmanagementsystem.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteRepository extends JpaRepository<Route, Long> {
    // Additional query methods can be defined here if needed
}
