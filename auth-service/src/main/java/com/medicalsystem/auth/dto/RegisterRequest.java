package com.medicalsystem.auth.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String fullName;
    private String role;

    // Backward-compatible fields used by existing frontend payloads
    private String email;
    private String name;

    public String resolveUsername() {
        if (username != null && !username.isBlank()) {
            return username;
        }
        return email;
    }

    public String resolveFullName() {
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }
        return name;
    }
}
