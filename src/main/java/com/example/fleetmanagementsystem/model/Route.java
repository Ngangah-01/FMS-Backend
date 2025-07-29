package com.example.fleetmanagementsystem.model;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long routeId;

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


//    @OneToOne
//    @JoinColumn(name="matatu_id")
//    private Matatu matatu;

//    @OneToMany(mappedBy = "route")
//    private List<Matatu> matatus = new ArrayList<>();

    //number of vehicles on one route

}
