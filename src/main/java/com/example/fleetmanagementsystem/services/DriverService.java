package com.example.fleetmanagementsystem.services;

import com.example.fleetmanagementsystem.model.Driver;
import com.example.fleetmanagementsystem.repositories.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;
import java.util.List;
@Service
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @Transactional
    public Driver saveDriver(Driver driver) {
        driverRepository.save(driver);
        return driver;
    }

    @Transactional(readOnly = true)
    public Optional<Driver> getDriverByidNumber(Long driverId) {
        return driverRepository.findById(driverId);

    }

    @Transactional(readOnly = true)
    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    @Transactional
    public void deleteDriver(Long driverId) {
        driverRepository.deleteById(driverId);
    }

//    public Driver updateDriver(Long id, Driver updatedDriver) {
//        Optional<Driver> existingDriver = getDriverById(id);
//        if (existingDriver.isEmpty()) {
//            throw new RuntimeException("Driver not found with id: " + id);
//        }
//        Driver driver = existingDriver.get();
//        // Update fields if they are not null
//        if (updatedDriver.getName() != null) {
//            driver.setName(updatedDriver.getName());
//        }
//        if (updatedDriver.getLicenseNumber() != null) {
//            driver.setLicenseNumber(updatedDriver.getLicenseNumber());
//        }
//        return driverRepository.save(driver);
//    }
}
