package com.example.fleetmanagementsystem.repositories;

import com.example.fleetmanagementsystem.model.Marshall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MarshallRepository extends JpaRepository<Marshall, Long> {

//    void deleteMarshallId(Long marshallId);

}
