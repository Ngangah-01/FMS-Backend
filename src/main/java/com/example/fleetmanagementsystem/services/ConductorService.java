package com.example.fleetmanagementsystem.services;

import com.example.fleetmanagementsystem.model.Conductor;
import com.example.fleetmanagementsystem.repositories.ConductorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ConductorService {

    private final ConductorRepository conductorRepository;

    public ConductorService(ConductorRepository conductorRepository) {
        this.conductorRepository = conductorRepository;
    }

    @Transactional
    public Conductor saveConductor(Conductor conductor) {
        return conductorRepository.save(conductor);
    }

    @Transactional
    public Optional<Conductor> getConductorById(Long conductorId) {
        return conductorRepository.findById(conductorId);
    }



    public List<Conductor> getAllConductors() {
        return conductorRepository.findAll();
    }

    public void deleteConductor(Long conductorId) {
        conductorRepository.deleteById(conductorId);
    }
}