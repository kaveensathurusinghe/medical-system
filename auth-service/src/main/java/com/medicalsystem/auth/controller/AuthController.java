package com.medicalsystem.auth.controller;

import com.medicalsystem.auth.dto.AuthResponse;
import com.medicalsystem.auth.dto.LoginRequest;
import com.medicalsystem.auth.dto.RegisterRequest;
import com.medicalsystem.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @Value("${jwt.expiration}")
    private long jwtExpirationMillis;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping({"/register", "/register/doctor", "/register/patient"})
    public ResponseEntity<?> register(@RequestBody RegisterRequest req, HttpServletRequest request) {
        try {
            String uri = request.getRequestURI();
            if (uri.endsWith("/register/doctor")) {
                req.setRole("DOCTOR");
            } else if (uri.endsWith("/register/patient")) {
                req.setRole("PATIENT");
            }

            authService.register(req);
            return ResponseEntity.ok(Map.of("message", "Registered successfully"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("message", "Failed to provision user in identity provider"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        String token = authService.login(req);
        return ResponseEntity.ok(new AuthResponse("Bearer", token, jwtExpirationMillis));
    }
}
