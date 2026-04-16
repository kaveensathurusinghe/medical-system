package com.medicalsystem.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String tokenType;
    private String token;
    private long expiresIn;
}
