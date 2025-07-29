package com.example.fleetmanagementsystem.repositories;

import com.example.fleetmanagementsystem.model.Matatu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatatuRepository extends JpaRepository<Matatu, String> {
    List<Matatu> findByPlateNumber(String plateNumber);

    //List<Matatu> findByAvailable(boolean b);

    List<Matatu> findByStatus(String status);
}
