package com.medicalsystem.controller;

import com.medicalsystem.model.Doctor;
import com.medicalsystem.model.Patient;
import com.medicalsystem.repository.DoctorCategoryRepository;
import com.medicalsystem.repository.DoctorRepository;
import com.medicalsystem.repository.PatientRepository;
import com.medicalsystem.service.AuthorizationService;
import com.medicalsystem.service.SequenceGeneratorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/session")
public class SessionController {

    private final AuthorizationService authorizationService;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorCategoryRepository doctorCategoryRepository;
    private final SequenceGeneratorService sequenceGeneratorService;

    public SessionController(AuthorizationService authorizationService,
                             DoctorRepository doctorRepository,
                             PatientRepository patientRepository,
                             DoctorCategoryRepository doctorCategoryRepository,
                             SequenceGeneratorService sequenceGeneratorService) {
        this.authorizationService = authorizationService;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorCategoryRepository = doctorCategoryRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> currentSession(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AdminController.Message("ERROR", "Not authenticated"));
        }

        String role = resolveRole(authentication.getAuthorities());
        if (role == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AdminController.Message("ERROR", "No supported role found"));
        }

        Long userId = null;
        if ("ROLE_DOCTOR".equals(role)) {
            userId = ensureDoctorProfile(authentication);
        } else if ("ROLE_PATIENT".equals(role)) {
            userId = ensurePatientProfile(authentication);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("role", role);
        response.put("userId", userId);
        response.put("username", authentication.getName());

        return ResponseEntity.ok(response);
    }

    private String resolveRole(Collection<? extends GrantedAuthority> authorities) {
        if (authorities == null) {
            return null;
        }

        if (hasAuthority(authorities, "ROLE_ADMIN")) {
            return "ROLE_ADMIN";
        }
        if (hasAuthority(authorities, "ROLE_DOCTOR")) {
            return "ROLE_DOCTOR";
        }
        if (hasAuthority(authorities, "ROLE_PATIENT")) {
            return "ROLE_PATIENT";
        }

        return null;
    }

    private boolean hasAuthority(Collection<? extends GrantedAuthority> authorities, String role) {
        for (GrantedAuthority authority : authorities) {
            if (role.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private Long ensureDoctorProfile(Authentication authentication) {
        Long existingId = authorizationService.getCurrentDoctorId(authentication);
        if (existingId != null) {
            return existingId;
        }

        String email = authentication.getName();
        Doctor doctor = new Doctor();
        doctor.setId(sequenceGeneratorService.generateSequence("doctor_seq"));
        doctor.setEmail(email);
        doctor.setName(resolveDisplayName(authentication, email));
        doctor.setPhone("");
        doctor.setSpecialization(resolveDefaultSpecialization());
        doctorRepository.save(doctor);

        return doctorRepository.findByEmail(email)
                .map(Doctor::getId)
                .orElse(doctor.getId());
    }

    private Long ensurePatientProfile(Authentication authentication) {
        Long existingId = authorizationService.getCurrentPatientId(authentication);
        if (existingId != null) {
            return existingId;
        }

        String email = authentication.getName();
        Patient patient = new Patient();
        patient.setId(sequenceGeneratorService.generateSequence("patient_seq"));
        patient.setEmail(email);
        patient.setName(resolveDisplayName(authentication, email));
        patient.setPhone("");
        patientRepository.save(patient);

        return patientRepository.findByEmail(email)
                .map(Patient::getId)
                .orElse(patient.getId());
    }

    private String resolveDefaultSpecialization() {
        return doctorCategoryRepository.findAllByOrderByNameAsc().stream()
                .findFirst()
                .map(category -> category.getName())
                .orElse("General");
    }

    private String resolveDisplayName(Authentication authentication, String fallbackEmail) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Object nameClaim = jwtAuth.getToken().getClaims().get("name");
            if (nameClaim != null) {
                String name = nameClaim.toString().trim();
                if (!name.isEmpty()) {
                    return name;
                }
            }
        }

        if (fallbackEmail != null && fallbackEmail.contains("@")) {
            return fallbackEmail.substring(0, fallbackEmail.indexOf('@'));
        }

        return "User";
    }
}
