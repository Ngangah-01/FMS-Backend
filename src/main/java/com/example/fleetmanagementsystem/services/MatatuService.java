package com.example.fleetmanagementsystem.services;

import com.example.fleetmanagementsystem.model.Matatu;
import com.example.fleetmanagementsystem.repositories.MatatuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Optional<Matatu> getMatatuById(Long id) {
        return matatuRepository.findById(id);
//                .orElseThrow(() -> new RuntimeException("Matatu not found with id: " + id));
    }

    //method to save a matatu
    public Matatu saveMatatu(Matatu matatu) {
        if (matatu.getRegNo() == null || matatu.getModel() == null || matatu.getCapacity() == null) {
            throw new IllegalArgumentException("Matatu registration number, model, and capacity must not be null");
        }
        return matatuRepository.save(matatu);

    }

    //method to update a matatu
    @Transactional
    public Matatu updateMatatu(Long id, Matatu updatedMatatu) {
        Optional<Matatu> existingMatatu = getMatatuById(id);
        if (existingMatatu.isEmpty()) {
            throw new RuntimeException("Matatu not found with id: " + id);
        }
        Matatu matatu = existingMatatu.get();
        // Update fields if they are not null
        if (updatedMatatu.getRegNo() != null) {
            matatu.setRegNo(updatedMatatu.getRegNo());
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
    public void deleteMatatu(Long id) {
        if (!matatuRepository.existsById(id)) {
            throw new RuntimeException("Matatu not found with id: " + id);
        }
        matatuRepository.deleteById(id);
    }

    //method to find matatus by registration number
    public List<Matatu> findMatatusByRegNo(String regNo) {
        return matatuRepository.findByRegNo(regNo);
    }

    //find available matatus
    public List<Matatu> findAvailableMatatus() {
        return matatuRepository.findByAvailable(true);
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
