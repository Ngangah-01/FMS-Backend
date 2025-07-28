package com.example.fleetmanagementsystem.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    @Column(name = "id_number")
    private Long idNumber;

    @Column(name  = "first_name", nullable = false)
    private String firstname;

    @Column(name = "last_name", nullable = false)
    private String lastname;

    @Column(nullable = false)
    private String password;

    @Email(message = "Invalid email format")
    @Column(name="email", unique = true,nullable = false)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

//    @Column(name = "role", nullable = false)
//    private String role; // Assuming a single role for simplicity, can be changed to Set<String> if multiple role are needed

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "id_number"))
    @Column(name = "role")
    private Set<String> roles;

    @Column(nullable = false)
    private boolean enabled = true; // Default value for enabled status

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Marshall marshall;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Driver driver;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Conductor conductor;

    public Set<String> getRole() {
        return roles != null ? roles : Collections.emptySet();
    }

    @Override
    public String toString() {
        return "Users(idNumber=" + idNumber + ", firstname=" + firstname + ", lastname=" + lastname +
                ", email=" + email + ", phoneNumber=" + phoneNumber + " , role=" + roles + ", marshall=" + marshall + ", driver=" + driver + ", conductor=" + conductor + ")";
    }

}
