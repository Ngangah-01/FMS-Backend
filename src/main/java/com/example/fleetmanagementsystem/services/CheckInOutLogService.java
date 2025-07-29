package com.example.fleetmanagementsystem.services;

import com.example.fleetmanagementsystem.model.CheckInOutLog;
import com.example.fleetmanagementsystem.model.Matatu;
import com.example.fleetmanagementsystem.model.Route;
import com.example.fleetmanagementsystem.repositories.CheckInOutLogRepository;
import com.example.fleetmanagementsystem.repositories.MatatuRepository;
import com.example.fleetmanagementsystem.repositories.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CheckInOutLogService {
    //This service will handle logic for check-in and check-out logs for one matatu
    private final CheckInOutLogRepository checkInOutLogRepository;
    private final MatatuRepository matatuRepository;
    private final RouteRepository routeRepository;

    @Autowired
    public CheckInOutLogService(CheckInOutLogRepository checkInOutLogRepository
            , MatatuRepository matatuRepository,
                                RouteRepository routeRepository) {
        this.checkInOutLogRepository = checkInOutLogRepository;
        this.matatuRepository = matatuRepository;
        this.routeRepository = routeRepository;
    }

    //Check-in
    public CheckInOutLog checkInMatatu(String plateNumber){
        //Find matatu
        Matatu matatu = matatuRepository.findById(plateNumber)
                .orElseThrow(()-> new RuntimeException("Matatu not found"));

        Route route = matatu.getRoute();
        if (route==null){
            throw new RuntimeException("Matatu not assigned to any route");
        }

        if (matatu.getStatus().equals("in-maintenance")){
            throw new RuntimeException("Matatu is under maintenance");
        }

        String stage = matatu.getCurrentStage();
        if (stage==null){
            stage = route.getStartPoint();
            matatu.setCurrentStage(stage);
            matatuRepository.save(matatu);
        }

        matatu.setTrip(1+ matatu.getTrip());
        matatu.setStatus("Boarding");

        //check if no checkout
        checkInOutLogRepository.findByMatatuPlateNumberAndStageNameAndCheckOutTimeIsNull(plateNumber, stage)
                .ifPresent(checkInOutLog -> {
                    throw new IllegalStateException("Matatu " + plateNumber + " is already checked in stage ");
                });

        //Create check-in log
        CheckInOutLog log = new CheckInOutLog();
        log.setMatatu(matatu);
        log.setStageName(stage);
        log.setCheckInTime(LocalDateTime.now());

        int previousTrip = checkInOutLogRepository.countByMatatuPlateNumberAndCheckOutTimeIsNotNull(plateNumber);
        log.setTrip(previousTrip+1);



        return checkInOutLogRepository.save(log);
    }

    //Check-out

    public CheckInOutLog checkOutMatatu(String plateNumber){
        Matatu matatu = matatuRepository.findById(plateNumber)
                .orElseThrow(()-> new RuntimeException("Matatu not found"));

        Route route = matatu.getRoute();
        if (route==null){
            throw new RuntimeException("Matatu not assigned to any route");
        }

        if (matatu.getStatus().equals("in-maintenance")){
            throw new RuntimeException("Matatu is under maintenance");
        }

        String stage = matatu.getCurrentStage();

        // Find the latest check-in log without a checkout time
        CheckInOutLog log = checkInOutLogRepository.findByMatatuPlateNumberAndStageNameAndCheckOutTimeIsNull(plateNumber, stage)
                .orElseThrow(() -> new IllegalStateException("Matatu " + plateNumber + " is not currently checked in"));

        //log check-out
        log.setCheckOutTime(LocalDateTime.now());

        if (stage.equals(route.getStartPoint())){
            matatu.setCurrentStage(route.getEndPoint());
        }else {
            matatu.setCurrentStage(route.getStartPoint());
        }
        matatu.setStatus("enroute");
        matatuRepository.save(matatu);

        return checkInOutLogRepository.save(log);
    }

    //Get currentStatus

    //Get matatu history




}
