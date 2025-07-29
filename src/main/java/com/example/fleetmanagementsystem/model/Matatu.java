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


    @ManyToOne
    @JoinColumn(name="route")
    private Route route;

    @Column
    private String currentStage;


//    public String getRouteName() {
//        return this.route != null ? this.route.getName() : null;
//    }


}