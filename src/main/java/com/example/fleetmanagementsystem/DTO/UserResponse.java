package com.example.fleetmanagementsystem.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UserResponse {
    private Long idNumber;
    private String firstname;
    private String lastname;
    private String email;
    private String phoneNumber;
    private String role;
//    private Set<String> roles;
    private boolean enabled;
    private String stage; // For MARSHALL
    private String licenseNumber; // For DRIVER
    // Exclude password for security

    @Override
    public String toString() {
        return "UserResponseDTO(idNumber=" + idNumber + ", firstname=" + firstname + ", lastname=" + lastname +
                ", email=" + email + ", phoneNumber=" + phoneNumber + ", role=" + role +
                ", enabled=" + enabled + ", stage=" + stage + ", licenseNumber=" + licenseNumber + ")";
    }
}