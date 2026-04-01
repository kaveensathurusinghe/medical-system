package com.medicalsystem.controller;

import com.medicalsystem.model.Patient;
import com.medicalsystem.service.AuthorizationService;
import com.medicalsystem.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private AuthorizationService authorizationService;

    @GetMapping
    public ResponseEntity<?> getAllPatients(Authentication authentication) {
        if (!authorizationService.isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AdminController.Message("ERROR", "Only admin can list all patients"));
        }
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @PostMapping("/register")
    public ResponseEntity<Patient> registerPatient(@RequestBody Patient patient) {
        return ResponseEntity.ok(patientService.savePatient(patient));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePatient(@PathVariable Long id,
                                           @RequestBody Patient patientDetails,
                                           Authentication authentication) {
        if (!authorizationService.canAccessPatient(authentication, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AdminController.Message("ERROR", "You can only update your own patient profile"));
        }

        Patient patient = patientService.getPatientById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + id));

        patient.setName(patientDetails.getName());
        patient.setDateOfBirth(patientDetails.getDateOfBirth());
        patient.setGender(patientDetails.getGender());
        patient.setAddress(patientDetails.getAddress());
        patient.setPhone(patientDetails.getPhone());
        patient.setEmail(patientDetails.getEmail());

        return ResponseEntity.ok(patientService.savePatient(patient));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPatientById(@PathVariable Long id,
                                            Authentication authentication) {
        if (!authorizationService.canAccessPatient(authentication, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AdminController.Message("ERROR", "You can only view your own patient profile"));
        }

        return patientService.getPatientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}