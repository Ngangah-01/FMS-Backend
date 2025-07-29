package com.example.fleetmanagementsystem.repositories;

import com.example.fleetmanagementsystem.model.CheckInOutLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CheckInOutLogRepository extends JpaRepository<CheckInOutLog, String> {

    Optional<CheckInOutLog> findByMatatuPlateNumberAndCheckInTimeIsNull(String plateNumber);
    Optional<CheckInOutLog> findByMatatuPlateNumberAndCheckOutTimeIsNull(String plateNumber);

    int countByMatatuPlateNumberAndCheckOutTimeIsNotNull(String plateNumber);

    Optional<CheckInOutLog> findByMatatuPlateNumberAndStageNameAndCheckOutTimeIsNull(String plateNumber, String stageName);
}
