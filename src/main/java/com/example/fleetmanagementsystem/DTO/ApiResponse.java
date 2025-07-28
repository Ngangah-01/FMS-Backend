package com.example.fleetmanagementsystem.DTO;


import lombok.*;
import lombok.Setter;

@Setter
@Getter
@Data

public class ApiResponse<T> {
    // Getters and setters
    private int status; // 1 for success, 0 for failure
    private String message;
    private T data;


    // Constructor for success response
    public ApiResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // Constructor for error response (no data)
    public ApiResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.data = null;
    }

}