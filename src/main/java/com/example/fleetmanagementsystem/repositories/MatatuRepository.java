package com.example.fleetmanagementsystem.repositories;

import com.example.fleetmanagementsystem.model.Matatu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatatuRepository extends JpaRepository<Matatu, Long> {
    List<Matatu> findByRegNo(String regNo);

    List<Matatu> findByAvailable(boolean b);
}
