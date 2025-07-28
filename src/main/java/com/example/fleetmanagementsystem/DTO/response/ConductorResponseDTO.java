package com.example.fleetmanagementsystem.DTO.response;

import com.example.fleetmanagementsystem.model.Conductor;
import lombok.Data;

@Data
public class ConductorResponseDTO {
    private Long conductorId;
    private String firstname;
    private String lastname;
    private String phoneNumber;
    private String email;

    public static ConductorResponseDTO from(Conductor conductor) {
        ConductorResponseDTO responseDTO = new ConductorResponseDTO();
        responseDTO.setConductorId(conductor.getConductorId());
        responseDTO.setFirstname(conductor.getFirstname());
        responseDTO.setLastname(conductor.getLastname());
        responseDTO.setPhoneNumber(conductor.getPhoneNumber());
        responseDTO.setEmail(conductor.getEmail());
        return responseDTO;
    }
}
