package com.example.fleetmanagementsystem.DTO;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
public class ChangePasswordDTO {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "New password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one digit, and one special character.")
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}
