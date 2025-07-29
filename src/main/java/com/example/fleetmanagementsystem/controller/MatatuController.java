package com.example.fleetmanagementsystem.controller;


import com.example.fleetmanagementsystem.DTO.ApiResponse;
import com.example.fleetmanagementsystem.model.CheckInOutLog;
import com.example.fleetmanagementsystem.model.Matatu;
import com.example.fleetmanagementsystem.model.Route;
import com.example.fleetmanagementsystem.repositories.RouteRepository;
import com.example.fleetmanagementsystem.services.CheckInOutLogService;
import com.example.fleetmanagementsystem.services.MatatuService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/matatus")
public class MatatuController {

    @Autowired
    private  MatatuService matatuService;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private CheckInOutLogService checkInOutLogService;

    //matatu DTO
    @Data
    public static class MatatuDTO {
        @NotBlank(message = "Plate number is required")
        @Size(min = 5, max = 10, message = "Plate number must be between 5 and 10 characters")
        private String plateNumber;

        @Min(value = 14, message = "Capacity must be at least 14")
        private int capacity;

        @NotBlank(message = "Model is required")
        @Size(max = 20, message = "Model must not exceed 20 characters")
        private String model;

        private String status;

        private String route;
    }


    @PreAuthorize("hasAnyRole('MARSHALL', 'ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Matatu>>> getAllMatatus() {
        List<Matatu> matatus = matatuService.getAllMatatus();
        return ResponseEntity.ok(new ApiResponse<>(1, "Matatus retrieved successfully", matatus));
    }

    @PreAuthorize("hasAnyRole('MARSHALL', 'ADMIN')")
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<Matatu>>> findAvailableMatatus() {
        List<Matatu> matatus = matatuService.findAvailableMatatus("available");
        return ResponseEntity.ok(new ApiResponse<>(1, "Available matatus retrieved successfully", matatus));
    }

    //create matatu
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<Matatu>> createMatatu(@Valid @RequestBody MatatuDTO matatuDTO) {
        try {

            Optional<Route> route = routeRepository.findByName(matatuDTO.getRoute());
            if (route.isEmpty()) {
                throw new IllegalArgumentException("Matatu route '" + matatuDTO.getRoute() + "' does not exist.");
            }

            Matatu newMatatu = new Matatu();
            newMatatu.setPlateNumber(matatuDTO.getPlateNumber().toUpperCase()); // Convert plateNumber to uppercase
            newMatatu.setCapacity(matatuDTO.getCapacity());
            newMatatu.setModel(matatuDTO.getModel());
            newMatatu.setStatus(matatuDTO.getStatus().toLowerCase());
            newMatatu.setRoute(route.get());
            newMatatu.setTrip(0);

            Matatu savedMatatu = matatuService.saveMatatu(newMatatu);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ApiResponse<>(1,"Matatu created successfully", savedMatatu)
            );
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(0, "Matatu with registration number " + matatuDTO.getPlateNumber() + " already exists", null)
            );
        } catch (IllegalArgumentException e){
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(0, e.getMessage(), null));
        }


    }

    //get matatu details
    @PreAuthorize("hasAnyRole('MARSHALL', 'ADMIN')")
    @GetMapping("/{plateNumber}")
    public Object getMatatuByPlateNumber(@PathVariable("plateNumber") String plateNumber) {
        if (plateNumber == null) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(0, "Invalid Matatu plate number", null)
            );
        }
        Optional<Matatu> matatu = matatuService.getMatatuByPlateNumber(plateNumber);
        return matatu.map(value -> ResponseEntity.ok(
                        new ApiResponse<>(1, "Matatu retrieved successfully", value)
                ))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new ApiResponse<>(0, "Matatu not found with plate number: " + plateNumber, null)
                ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{plateNumber}")
    public ResponseEntity<ApiResponse<Matatu>> updateMatatu(
            @PathVariable("plateNumber") String plateNumber,
            @Valid @RequestBody MatatuDTO matatuDTO) {
        if (plateNumber == null) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(0, "Invalid Matatu plate number", null));
        }
        Optional<Matatu> existingMatatu = matatuService.getMatatuByPlateNumber(plateNumber);
        if (existingMatatu.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(0, "Matatu not found with plate number: " + plateNumber, null)
            );
        }
        Matatu matatu = existingMatatu.get();
        matatu.setPlateNumber(matatuDTO.getPlateNumber().toUpperCase());
        matatu.setCapacity(matatuDTO.getCapacity());
        matatu.setModel(matatuDTO.getModel());
        matatu.setStatus(matatuDTO.getStatus());

        //matatu.setRouteName(matatuDTO.getRouteName());

//        matatu.setAvailable(matatuDTO.getAvailable() != null ? matatuDTO.getAvailable() : matatu.getAvailable());
        Matatu updatedMatatu = matatuService.saveMatatu(matatu);
        return ResponseEntity.ok(
                new ApiResponse<>(1, "Matatu updated successfully", updatedMatatu)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{plateNumber}")
    public ResponseEntity<ApiResponse<Object>> deleteMatatu(@PathVariable("plateNumber") String plateNumber) {
        if (plateNumber == null) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(0, "Invalid Matatu ID", null)
            );
        }
        Optional<Matatu> matatu = matatuService.getMatatuByPlateNumber(plateNumber);
        if (matatu.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(0, "Matatu not found with plate number: " + plateNumber, null)
            );
        }
        matatuService.deleteMatatu(plateNumber);
        return ResponseEntity.ok(
                new ApiResponse<>(1, "Matatu deleted successfully", null)
        );
    }

    //count
    @PreAuthorize("hasAnyRole('MARSHALL', 'ADMIN')")
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getMatatuCount() {
        long count = matatuService.countMatatus();
        return ResponseEntity.ok(
                new ApiResponse<>(1, "Matatu count retrieved successfully", count)
        );
    }

    @PreAuthorize("hasAnyRole('MARSHALL', 'ADMIN')")
    @PostMapping("/{plateNumber}/check-in")
    public ResponseEntity<ApiResponse<CheckInOutLog>> checkInMatatu(
            @PathVariable("plateNumber") String plateNumber
    ){
        try {
            Optional<Matatu> existingMatatu = matatuService.getMatatuByPlateNumber(plateNumber);
            if (existingMatatu.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new ApiResponse<>(0, "Matatu not found with plate number: " + plateNumber, null)
                );
            }

            Matatu matatu = existingMatatu.get();
            plateNumber = matatu.getPlateNumber();
            CheckInOutLog checkInLog = checkInOutLogService.checkInMatatu(plateNumber);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ApiResponse<>(1, "Matatu checked in successfully", checkInLog));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(0, "Error: " + e.getMessage(), null)
            );
        }
    }

    @PreAuthorize("hasAnyRole('MARSHALL', 'ADMIN')")
    @PostMapping("/{plateNumber}/check-out")
    public ResponseEntity<ApiResponse<CheckInOutLog>> checkOutMatatu(
            @PathVariable("plateNumber") String plateNumber
    ){
        try {
            Optional<Matatu> existingMatatu = matatuService.getMatatuByPlateNumber(plateNumber);
            if (existingMatatu.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new ApiResponse<>(0, "Matatu not found with plate number: " + plateNumber, null)
                );
            }

            Matatu matatu = existingMatatu.get();
            plateNumber = matatu.getPlateNumber();
            CheckInOutLog checkInLog = checkInOutLogService.checkOutMatatu(plateNumber);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ApiResponse<>(1, "Matatu checked out successfully", checkInLog));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(0, "Error: " + e.getMessage(), null)
            );
        }
    }
}
