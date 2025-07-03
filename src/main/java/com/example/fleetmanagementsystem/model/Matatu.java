package com.example.fleetmanagementsystem.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Matatus")

public class Matatu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reg_no" ,unique = true)
    private String regNo;

    private Integer capacity;

    private String model;

    boolean available = true; // Default value for availability

}