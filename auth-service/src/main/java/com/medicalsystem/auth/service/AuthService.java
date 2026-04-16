package com.medicalsystem.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.medicalsystem.auth.dto.LoginRequest;
import com.medicalsystem.auth.dto.RegisterRequest;
import com.medicalsystem.auth.model.User;
import com.medicalsystem.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KeycloakAdminService keycloakAdminService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMillis;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, KeycloakAdminService keycloakAdminService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.keycloakAdminService = keycloakAdminService;
    }

    public void register(RegisterRequest req) {
        String username = req.resolveUsername();
        String fullName = req.resolveFullName();
        String role = normalizeRole(req.getRole());

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username/email is required");
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Keep Keycloak as the source of truth for login credentials and roles.
        keycloakAdminService.createUserWithRole(username, req.getPassword(), fullName, role);

        User u = new User();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setFullName(fullName);
        u.setRole(role);
        userRepository.save(u);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "PATIENT";
        }
        return role.toUpperCase();
    }

    public String login(LoginRequest req) {
        User u = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(req.getPassword(), u.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        Algorithm alg = Algorithm.HMAC256(jwtSecret);
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtExpirationMillis);
        return JWT.create()
                .withSubject(u.getUsername())
                .withClaim("role", u.getRole())
                .withIssuedAt(now)
                .withExpiresAt(exp)
                .sign(alg);
    }
}
