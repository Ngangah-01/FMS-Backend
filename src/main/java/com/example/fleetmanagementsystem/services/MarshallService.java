package com.example.fleetmanagementsystem.services;

import com.example.fleetmanagementsystem.model.Marshall;
import com.example.fleetmanagementsystem.repositories.MarshallRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MarshallService {

    private final MarshallRepository marshallProfileRepository;

    public MarshallService(MarshallRepository marshallProfileRepository) {
        this.marshallProfileRepository = marshallProfileRepository;
    }

    public Marshall saveMarshallProfile(Marshall marshall) {
        return marshallProfileRepository.save(marshall);
    }

    public Optional<Marshall> getMarshallProfileById(Long id) {
        return marshallProfileRepository.findById(id);
    }

    public List<Marshall> getAllMarshallProfiles() {
        return marshallProfileRepository.findAll();
    }

    public void deleteMarshallProfile(Long id) {
        marshallProfileRepository.deleteById(id);
    }
}