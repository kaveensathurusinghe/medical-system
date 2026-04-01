package com.medicalsystem.controller;

import com.medicalsystem.model.Doctor;
import com.medicalsystem.model.Patient;
import com.medicalsystem.service.DoctorService;
import com.medicalsystem.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    @PostMapping("/register/patient")
    public ResponseEntity<?> registerPatient(@RequestBody Patient patient) {
        try {
            Patient registeredPatient = patientService.registerPatient(
                    patient.getName(),
                    patient.getEmail(),
                    patient.getPassword(),
                    patient.getPhone(),
                    patient.getAge(),
                    patient.getGender()
            );
            return ResponseEntity.ok(registeredPatient);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AdminController.Message("ERROR", e.getMessage()));
        }
    }

    @PostMapping("/register/doctor")
    public ResponseEntity<?> registerDoctor(@RequestBody Doctor doctor) {
        try {
            Doctor registeredDoctor = doctorService.registerDoctor(
                    doctor.getName(),
                    doctor.getEmail(),
                    doctor.getPassword(),
                    doctor.getPhone(),
                    doctor.getSpecialization()
            );
            return ResponseEntity.ok(registeredDoctor);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AdminController.Message("ERROR", e.getMessage()));
        }
    }
}