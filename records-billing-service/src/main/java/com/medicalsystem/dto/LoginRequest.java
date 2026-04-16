package com.medicalsystem.dto;

import com.medicalsystem.model.UserType;
import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
    private UserType userType;
}
