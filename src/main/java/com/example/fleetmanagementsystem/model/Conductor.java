package com.example.fleetmanagementsystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@Entity
@Table(name = "conductors")
public class Conductor {

    @Id
    @Column(name = "conductor_ID")
    private Long conductorId;

    @NotBlank
    @Column(nullable = false)
    private String firstname;

    @NotBlank
    @Column(nullable = false)
    private String lastname;

    @Column(name="email", unique = true, nullable = false)
    private String Email;

    @Column
    private String PhoneNumber;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "conductor_ID", referencedColumnName = "id_Number",insertable = false,updatable = false, unique = true)
    private Users user;

    //getter and setter for email


}
