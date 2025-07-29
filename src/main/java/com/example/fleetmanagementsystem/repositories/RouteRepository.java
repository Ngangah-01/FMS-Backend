package com.example.fleetmanagementsystem.repositories;

import com.example.fleetmanagementsystem.model.Marshall;
import com.example.fleetmanagementsystem.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {
    // Additional query methods can be defined here if needed
    //Optional<Route> findByStartOrEndMarshall(Marshall marshall);
    //Optional<Route> findByStart_marshall_dOrEnd_marshall_id(Long marshallId);

    Optional<Route> findByStartMarshall_MarshallIdOrEndMarshall_MarshallId(Long startMarshallId, Long endMarshallId);

    //Optional<Route> findByMatatu_PlateNumber(String plateNumber);

    Optional<Route> findByName(String name);

}
