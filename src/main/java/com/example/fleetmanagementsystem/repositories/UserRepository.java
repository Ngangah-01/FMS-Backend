package com.example.fleetmanagementsystem.repositories;

import com.example.fleetmanagementsystem.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    // Additional query methods can be defined here if needed
//    Optional<Users> findByUsername(String username);
    Optional<Users> findByidNumber(Long idNumber);

    List<Users> findAllByRoles(String admin);

    Optional<Users> findByEmail(String email);

}
