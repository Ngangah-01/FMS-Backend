package com.example.fleetmanagementsystem.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Matatus")

public class Matatu {

    @Id
    @Column(name = "plate_number" ,unique = true)
    private String plateNumber;

    private Integer capacity;

    private String model;

    private String status; //available, assigned, enroute, breakdown, delay

    private String route;

//    boolean available = true; // Default value for availability

}