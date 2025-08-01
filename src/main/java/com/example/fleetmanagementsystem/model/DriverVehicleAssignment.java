package com.example.fleetmanagementsystem.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "driver_vehicle_assignments")
public class DriverVehicleAssignment {
    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "driver_id", nullable = false, unique = true)
    private Driver driver;

    @OneToOne
    @JoinColumn(name = "vehicle_id", nullable = false, unique = true)
    private Matatu matatu;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "assigned_by" , nullable = false)
    private String assignedBy;

    @PrePersist
    protected void onCreate() {
        assignedAt = LocalDateTime.now();
    }

//    public void setAssignedBy(String assignedBy) {
//        if (driver != null) {
//            driver.setAssignedBy(assignedBy);
//        } else {
//            throw new IllegalStateException("Driver must be set before setting assigned by");
//        }
    }
