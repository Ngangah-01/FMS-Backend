package com.example.fleetmanagementsystem.services;

import com.example.fleetmanagementsystem.model.Matatu;
import com.example.fleetmanagementsystem.repositories.MatatuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.util.List;
import java.util.Optional;

@Service
public class MatatuService {

    private final MatatuRepository matatuRepository;

    @Autowired
    public MatatuService(MatatuRepository matatuRepository) {
        this.matatuRepository = matatuRepository;
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
    public Matatu saveMatatu(Matatu matatu) {
        if (matatu.getPlateNumber() == null || matatu.getModel() == null || matatu.getCapacity() == null) {
            throw new IllegalArgumentException("Matatu registration number, model, and capacity must not be null");
        }
        return matatuRepository.save(matatu);

    }

    //method to update a matatu
    @Transactional
    public Matatu updateMatatu(String plateNumber, Matatu updatedMatatu) {
        Optional<Matatu> existingMatatu = getMatatuByPlateNumber(plateNumber);
        if (existingMatatu.isEmpty()) {
            throw new RuntimeException("Matatu not found with plateNumber: " + plateNumber);
        }
        Matatu matatu = existingMatatu.get();
        // Update fields if they are not null
        if (updatedMatatu.getPlateNumber() != null) {
            matatu.setPlateNumber(updatedMatatu.getPlateNumber());
        }
        if (updatedMatatu.getModel() != null) {
            matatu.setModel(updatedMatatu.getModel());
        }
        if (updatedMatatu.getCapacity() != null) {
            matatu.setCapacity(updatedMatatu.getCapacity());
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

    public Matatu saveMatatu(Optional<Matatu> matatu) {
        if (matatu.isEmpty()) {
            throw new IllegalArgumentException("Matatu must not be null");
        }
        return matatuRepository.save(matatu.get());
    }

    //find number of matatus
    public long countMatatus() {
        return matatuRepository.count();
    }
}
