package com.example.fleetmanagementsystem.repositories;

import com.example.fleetmanagementsystem.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    // Additional query methods can be defined here if needed
    Optional<Users> findByUsername(String username);

}
