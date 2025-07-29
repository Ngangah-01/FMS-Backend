package com.example.fleetmanagementsystem.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class CheckInOutLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "plate_number", referencedColumnName = "plate_number")
    private Matatu matatu;

    private String driverName;

    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    //This can be a controller
    private int trip; //Should be incremented on each check-in\

    private String stageName;




}
