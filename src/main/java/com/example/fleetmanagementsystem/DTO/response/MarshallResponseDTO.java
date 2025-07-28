package com.example.fleetmanagementsystem.DTO.response;

import com.example.fleetmanagementsystem.model.Marshall;
import lombok.Data;

@Data
public class MarshallResponseDTO {
    private Long marshallId;
    private String firstname;
    private String lastname;
    private String stage;
    private String phoneNumber;
    private String email;

    public static MarshallResponseDTO from(Marshall marshall) {
        MarshallResponseDTO response = new MarshallResponseDTO();
        response.setMarshallId(marshall.getMarshallId());
        response.setFirstname(marshall.getFirstname());
        response.setLastname(marshall.getLastname());
        response.setStage(marshall.getStage());
        response.setPhoneNumber(marshall.getPhoneNumber());
        response.setEmail(marshall.getEmail());
        return response;
    }
}
