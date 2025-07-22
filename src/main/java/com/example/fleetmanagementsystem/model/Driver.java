package com.example.fleetmanagementsystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
@Data
@Entity
@Table(name = "drivers")
public class Driver {

    @Id
    @Column(name = "Driver_ID")
    private Long driverId;

    @NotBlank
    @Column(nullable = false)
    private String firstname;

    @NotBlank
    @Column(nullable = false)
    private String lastname;

    // email should be unique
    @Column(unique = true, nullable = false)
    private String Email;

    @Column
    private String PhoneNumber;

    @Column(nullable = false, unique = true)
    private String licenseNumber;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "Driver_ID",referencedColumnName = "id_Number", unique = true)
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
