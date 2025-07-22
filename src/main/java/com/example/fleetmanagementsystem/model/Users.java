package com.example.fleetmanagementsystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "users")
public class Users {

    @Id
    @Column(name = "id_Number")
    private Long idNumber;


    @Column(name  = "first_name", nullable = false)
    private String firstname;

    @Setter
    @Getter
    @Column(name = "last_name", nullable = false)
    private String lastname;

    @Setter
    @Getter
    @Column(nullable = false)
    private String password;

    // Assuming username is used as email
    @Setter
    @Getter
    @Email(message = "Invalid email format")
    @Column(name="email", unique = true,nullable = false)
    private String email;

    @Setter
    @Getter
    @Column(name = "phone_number")
    private String phoneNumber;

    @Setter
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "id_number"))
    @Column(name = "role")
    private Set<String> roles;

    @Setter
    @Getter
    @Column(nullable = false)
    private boolean enabled = true; // Default value for enabled status

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Marshall marshall;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Driver driver;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Conductor conductor;

    public Set<String> getRoles() {
        return roles != null ? roles : Collections.emptySet();
    }

    @Override
    public String toString() {
        return "Users(idNumber=" + idNumber + ", firstname=" + firstname + ", lastname=" + lastname +
                ", email=" + email + ", phoneNumber=" + phoneNumber + ", enabled=" + enabled +
                ", role=" + roles + ", marshall=" + marshall + ", driver=" + driver + ", conductor=" + conductor + ")";
    }


}
