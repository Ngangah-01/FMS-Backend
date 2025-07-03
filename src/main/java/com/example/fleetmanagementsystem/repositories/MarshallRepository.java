package com.example.fleetmanagementsystem.repositories;

import com.example.fleetmanagementsystem.model.Marshall;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarshallRepository extends JpaRepository<Marshall, Long> {

    // Additional query methods can be defined here if needed,
    // For example, to find a Marshall by name:
    // List<Marshall> findByName(String name);
}
