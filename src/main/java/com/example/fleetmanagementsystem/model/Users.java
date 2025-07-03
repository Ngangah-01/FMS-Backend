package com.example.fleetmanagementsystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Entity
@Table(name = "users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Getter
    @Column(unique = true, nullable = false)
    private String username;

    @Setter
    @Getter
    @Column(nullable = false)
    private String password;

    // Assuming username is used as email
    @Setter
    @Getter
    @Email(message = "Invalid email format")
    @Column(unique = true, nullable = false)
    private String email;

    @Setter
    @Getter
    @Column(name = "phone_number")
    private String phoneNumber;

    @Setter
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles;

    @Setter
    @Getter
    @Column(nullable = false)
    private boolean enabled = true; // Default value for enabled status


    public Set<String> getRoles() {
        return roles != null ? roles : Collections.emptySet();
    }

    public Object getId() {
        return id;
    }


    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this.username = name; // Assuming username is used as name
    }

}
