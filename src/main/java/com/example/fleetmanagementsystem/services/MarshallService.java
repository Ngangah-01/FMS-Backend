package com.example.fleetmanagementsystem.services;

import com.example.fleetmanagementsystem.model.Marshall;
import com.example.fleetmanagementsystem.repositories.MarshallRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MarshallService {

    private final MarshallRepository marshallProfileRepository;

    public MarshallService(MarshallRepository marshallProfileRepository) {
        this.marshallProfileRepository = marshallProfileRepository;
    }

    @Transactional
    public Marshall saveMarshallProfile(Marshall marshall) {
        System.out.println("Saving Marshall Profile: " + marshall);
        if (marshall.getMarshallId() == null) {
            throw new IllegalArgumentException("ID Number must not be null");
        }
        return marshallProfileRepository.save(marshall);
    }

    @Transactional(readOnly = true)
    public Optional<Marshall> findByMarshallId(Long marshallId) {
        return marshallProfileRepository.findById(marshallId);
    }

    @Transactional(readOnly = true)
    public List<Marshall> getAllMarshallProfiles() {
        return marshallProfileRepository.findAll();
    }

    public void deleteMarshallProfile(Long marshallId) {
        if (marshallId == null) {
            throw new IllegalArgumentException("ID Number must not be null");
        }
        marshallProfileRepository.deleteById(marshallId);
    }
}