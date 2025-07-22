package com.example.fleetmanagementsystem.model;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String startPoint;

    @Column(nullable = false)
    private String endPoint;

    @ManyToOne
    @JoinColumn(name = "start_marshall_id")
    private Marshall startMarshall;

    @ManyToOne
    @JoinColumn(name="end_marshall_id")
    private Marshall endMarshall;


    //number of vehicles on one route

    //stage marshalls on each point
}
