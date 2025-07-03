package com.example.fleetmanagementsystem.services;

import com.example.fleetmanagementsystem.model.Conductor;
import com.example.fleetmanagementsystem.repositories.ConductorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConductorService {

    private final ConductorRepository conductorRepository;

    public ConductorService(ConductorRepository conductorRepository) {
        this.conductorRepository = conductorRepository;
    }

    public Conductor saveConductor(Conductor conductor) {
        return conductorRepository.save(conductor);
    }

//    public Optional<Conductor> findByEmail(String email){
//        return conductorRepository.findByEmail(email);
//    }

    public Optional<Conductor> getConductorById(Long id) {
        return conductorRepository.findById(id);
    }



    public List<Conductor> getAllConductors() {
        return conductorRepository.findAll();
    }

    public void deleteConductor(Long id) {
        conductorRepository.deleteById(id);
    }
}