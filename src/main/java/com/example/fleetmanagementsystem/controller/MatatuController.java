package com.example.fleetmanagementsystem.controller;


import com.example.fleetmanagementsystem.DTO.ApiResponse;
import com.example.fleetmanagementsystem.model.Matatu;
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

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/matatus")
public class MatatuController {

    @Autowired
    private  MatatuService matatuService;

    //matatu DTO
    @Data
    public static class MatatuDTO {
        @NotBlank(message = "Registration number is required")
        @Size(min = 5, max = 10, message = "Registration number must be between 5 and 10 characters")
        private String regNo;

        @Min(value = 14, message = "Capacity must be at least 14")
        private int capacity;

        @NotBlank(message = "Model is required")
        @Size(max = 20, message = "Model must not exceed 20 characters")
        private String model;
    }


    @PreAuthorize("hasAnyRole('MARSHALL', 'ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Matatu>>> getAllMatatus() {
        List<Matatu> matatus = matatuService.getAllMatatus();
        return ResponseEntity.ok(new ApiResponse<>(1, "Matatus retrieved successfully", matatus));
    }

    @PreAuthorize("hasAnyRole('MARSHALL', 'ADMIN')")
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<Matatu>>> getAvailableMatatus() {
        List<Matatu> matatus = matatuService.findAvailableMatatus();
        return ResponseEntity.ok(new ApiResponse<>(1, "Available matatus retrieved successfully", matatus));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<Matatu>> createMatatu(@Valid @RequestBody MatatuDTO matatuDTO) {
        try {
            Matatu newMatatu = new Matatu();
            newMatatu.setRegNo(matatuDTO.getRegNo().toUpperCase()); // Convert regNo to uppercase
            newMatatu.setCapacity(matatuDTO.getCapacity());
            newMatatu.setModel(matatuDTO.getModel());
            Matatu savedMatatu = matatuService.saveMatatu(newMatatu);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ApiResponse<>(1,"Matatu created successfully", savedMatatu)
            );
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(0, "Matatu with registration number " + matatuDTO.getRegNo() + " already exists", null)
            );
        }
    }

    @PreAuthorize("hasAnyRole('MARSHALL', 'ADMIN')")
    @GetMapping("/{id}")
    public Object getMatatuById(@PathVariable("id") Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(0, "Invalid Matatu ID", null)
            );
        }
        Optional<Matatu> matatu = matatuService.getMatatuById(id);
        return matatu.map(value -> ResponseEntity.ok(
                        new ApiResponse<>(1, "Matatu retrieved successfully", value)
                ))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new ApiResponse<>(0, "Matatu not found with ID: " + id, null)
                ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Matatu>> updateMatatu(
            @PathVariable("id") Long id,
            @Valid @RequestBody MatatuDTO matatuDTO) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(0, "Invalid Matatu ID", null));
        }
        Optional<Matatu> existingMatatu = matatuService.getMatatuById(id);
        if (existingMatatu.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(0, "Matatu not found with ID: " + id, null)
            );
        }
        Matatu matatu = existingMatatu.get();
        matatu.setRegNo(matatuDTO.getRegNo().toUpperCase());
        matatu.setCapacity(matatuDTO.getCapacity());
        matatu.setModel(matatuDTO.getModel());
//        matatu.setAvailable(matatuDTO.getAvailable() != null ? matatuDTO.getAvailable() : matatu.getAvailable());
        Matatu updatedMatatu = matatuService.saveMatatu(matatu);
        return ResponseEntity.ok(
                new ApiResponse<>(1, "Matatu updated successfully", updatedMatatu)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteMatatu(@PathVariable("id") Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(0, "Invalid Matatu ID", null)
            );
        }
        Optional<Matatu> matatu = matatuService.getMatatuById(id);
        if (matatu.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(0, "Matatu not found with ID: " + id, null)
            );
        }
        matatuService.deleteMatatu(id);
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
}
