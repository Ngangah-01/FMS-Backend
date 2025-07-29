package com.example.fleetmanagementsystem.services;

import com.example.fleetmanagementsystem.model.Matatu;
import com.example.fleetmanagementsystem.model.Route;
import com.example.fleetmanagementsystem.repositories.MatatuRepository;
import com.example.fleetmanagementsystem.repositories.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.util.List;
import java.util.Optional;

@Service
public class MatatuService {

    private final MatatuRepository matatuRepository;
    private final RouteRepository routeRepository;

    @Autowired
    public MatatuService(MatatuRepository matatuRepository
    , RouteRepository routeRepository) {
        this.matatuRepository = matatuRepository;
        this.routeRepository = routeRepository;
    }

    //method to get all matatus
    public List<Matatu> getAllMatatus() {
        return matatuRepository.findAll();
    }

    //method to get a matatu by id
    public Optional<Matatu> getMatatuByPlateNumber(String plateNumber) {
        return matatuRepository.findById(plateNumber);
//                .orElseThrow(() -> new RuntimeException("Matatu not found with id: " + id));
    }

    //method to save a matatu
    @Transactional
    public Matatu saveMatatu(Matatu matatu) {

//        // Validate route
//        Optional<Route> route = routeRepository.findByName(matatu.getRoute());
//        if (route.isEmpty()) {
//            throw new IllegalArgumentException("Matatu route does not exist");
//        }

        if (matatu.getRoute() == null || matatu.getRoute().getRouteId() == null) {
            throw new IllegalArgumentException("Route must not be null and must have a valid ID");
        }
        if (!routeRepository.existsById(matatu.getRoute().getRouteId())) {
            throw new IllegalArgumentException("Route with ID " + matatu.getRoute().getRouteId() + " does not exist");
        }
        if (matatu.getPlateNumber() == null || matatu.getPlateNumber().isBlank()) {
            throw new IllegalArgumentException("Plate number must not be null or empty");
        }
        if (matatu.getModel() == null || matatu.getModel().isBlank()) {
            throw new IllegalArgumentException("Model must not be null or empty");
        }
        if (matatu.getCapacity() == null || matatu.getCapacity() <= 0) {
            throw new IllegalArgumentException("Capacity must be a positive number");
        }
        if (matatuRepository.findById(matatu.getPlateNumber()).isPresent()) {
            throw new IllegalArgumentException("Matatu with plate number '" + matatu.getPlateNumber() + "' already exists");
        }
//        matatu.setRoute(route.get());
        return matatuRepository.save(matatu);
    }

    //method to update a matatu
    @Transactional
    public Matatu updateMatatu(Matatu matatu) {
        if (matatu.getRoute() == null || matatu.getRoute().getRouteId() == null) {
            throw new IllegalArgumentException("Route must not be null and must have a valid ID");
        }
        if (!routeRepository.existsById(matatu.getRoute().getRouteId())) {
            throw new IllegalArgumentException("Route with ID " + matatu.getRoute().getRouteId() + " does not exist");
        }
        if (matatu.getPlateNumber() == null || matatu.getPlateNumber().isBlank()) {
            throw new IllegalArgumentException("Plate number must not be null or empty");
        }
        if (matatu.getModel() == null || matatu.getModel().isBlank()) {
            throw new IllegalArgumentException("Model must not be null or empty");
        }
        if (matatu.getCapacity() == null || matatu.getCapacity() <= 0) {
            throw new IllegalArgumentException("Capacity must be a positive number");
        }
        if (matatuRepository.findById(matatu.getPlateNumber()).isEmpty()) {
            throw new IllegalArgumentException("Matatu with plate number '" + matatu.getPlateNumber() + "' does not exist");
        }
        return matatuRepository.save(matatu);
    }

    //method to delete a matatu
    public void deleteMatatu(String plateNumber){
        if (!matatuRepository.existsById(plateNumber)){
            throw new RuntimeException("Matatu not found with plateNumber: " + plateNumber);
        }
        matatuRepository.deleteById(plateNumber);
    }

    //method to find matatus by registration number
    public List<Matatu> findMatatusByPlateNumber(String plateNumber) {
        return matatuRepository.findByPlateNumber(plateNumber);
    }

//    //find available matatus
//    public List<Matatu> findAvailableMatatus() {
//        return matatuRepository.findByAvailable(true);
//    }

    //find available matatus
    public List<Matatu> findAvailableMatatus(String status){
        return matatuRepository.findByStatus("available");
    }

//    public Matatu saveMatatu(Optional<Matatu> matatu) {
//        if (matatu.isEmpty()) {
//            throw new IllegalArgumentException("Matatu must not be null");
//        }
//        return matatuRepository.save(matatu.get());
//    }

    //find number of matatus
    public long countMatatus() {
        return matatuRepository.count();
    }


}
