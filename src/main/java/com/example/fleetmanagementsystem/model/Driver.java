package com.example.fleetmanagementsystem.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Collection;


@Data
@Entity
@Table(name = "drivers")
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String Name;

    // email should be unique
    @Column(unique = true, nullable = false)
    private String Email;

    @Column
    private String PhoneNumber;

    @Column(nullable = false, unique = true)
    private String licenseNumber;

    @OneToOne
    @JoinColumn(name = "user_id",referencedColumnName = "id", unique = true)
    private Users user; // Assuming Users is a class that represents the user details


    public void setPassword(String encode) {
        if (user != null) {
            user.setPassword(encode);
            return;
        } else {
            throw new IllegalStateException("User must be set before setting password");
        }

    }

    public String getRoles(){
        if (user != null) {
            return user.getRoles().toString();
        } else {
            throw new IllegalStateException("User must be set to get role");
        }
    }

    // Additional fields can be added as needed
}
