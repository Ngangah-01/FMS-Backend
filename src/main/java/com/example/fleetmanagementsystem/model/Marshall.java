package com.example.fleetmanagementsystem.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "marshalls")
public class Marshall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String Name;

    //email should be unique
    @Column(unique = true, nullable = false)
    private String Email;

    @Column
    private String PhoneNumber;

    @Column(nullable = false)
    private String Stage;

    @ManyToOne
    private Route route;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private Users user; // Assuming Users is a class that represents the user details



    // Additional fields can be added as needed

}
