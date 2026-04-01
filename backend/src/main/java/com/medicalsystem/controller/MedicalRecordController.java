package com.medicalsystem.controller;

import com.medicalsystem.dto.MedicalRecordRequest;
import com.medicalsystem.model.Appointment;
import com.medicalsystem.model.MedicalRecord;
import com.medicalsystem.service.AppointmentService;
import com.medicalsystem.service.AuthorizationService;
import com.medicalsystem.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/records")
public class MedicalRecordController {

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AuthorizationService authorizationService;

    @PostMapping
    public ResponseEntity<?> createMedicalRecord(@RequestBody MedicalRecordRequest request,
                                                 Authentication authentication) {
        try {
            Long appointmentId = Long.valueOf(request.getAppointmentId());
            Appointment appointment = appointmentService.getAppointmentById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));

            if (!authorizationService.canAccessDoctor(authentication, appointment.getDoctorId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new AdminController.Message("ERROR", "You can only create records for your own appointments"));
            }

            if (medicalRecordService.getRecordByAppointmentId(appointmentId).isPresent()) {
                return ResponseEntity.badRequest()
                        .body(new AdminController.Message("ERROR", "Medical record already exists for this appointment"));
            }

            MedicalRecord record = new MedicalRecord();
            record.setAppointmentId(appointmentId);
            record.setPatientId(appointment.getPatientId());
            record.setDoctorId(appointment.getDoctorId());
            record.setRecordDate(LocalDateTime.now());
            record.setDiagnosis(request.getDiagnosis());
            record.setTreatment(request.getTreatment());
            record.setNotes(request.getNotes());

            medicalRecordService.createRecord(record);
            return ResponseEntity.ok(new AdminController.Message("SUCCESS", "Medical record created successfully"));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(new AdminController.Message("ERROR", "Invalid appointment ID"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AdminController.Message("ERROR", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllRecords(Authentication authentication) {
        if (authorizationService.isAdmin(authentication)) {
            return ResponseEntity.ok(medicalRecordService.getAllRecords());
        }

        if (authorizationService.isDoctor(authentication)) {
            Long doctorId = authorizationService.getCurrentDoctorId(authentication);
            if (doctorId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new AdminController.Message("ERROR", "Doctor account not found"));
            }
            return ResponseEntity.ok(medicalRecordService.getRecordsByDoctorId(doctorId));
        }

        if (authorizationService.isPatient(authentication)) {
            Long patientId = authorizationService.getCurrentPatientId(authentication);
            if (patientId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new AdminController.Message("ERROR", "Patient account not found"));
            }
            return ResponseEntity.ok(medicalRecordService.getRecordsByPatientId(patientId));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new AdminController.Message("ERROR", "Not authorized to view medical records"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRecordById(@PathVariable Long id,
                                           Authentication authentication) {
        MedicalRecord record = medicalRecordService.getRecordById(id).orElse(null);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }

        boolean allowed = authorizationService.isAdmin(authentication)
                || authorizationService.canAccessDoctor(authentication, record.getDoctorId())
                || authorizationService.canAccessPatient(authentication, record.getPatientId());

        if (!allowed) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AdminController.Message("ERROR", "You are not allowed to view this medical record"));
        }

        return ResponseEntity.ok(record);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getRecordsByPatientId(@PathVariable Long patientId,
                                                   Authentication authentication) {
        if (authorizationService.isAdmin(authentication)) {
            return ResponseEntity.ok(medicalRecordService.getRecordsByPatientId(patientId));
        }

        if (authorizationService.isPatient(authentication)) {
            if (!authorizationService.canAccessPatient(authentication, patientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new AdminController.Message("ERROR", "You can only view your own medical records"));
            }
            return ResponseEntity.ok(medicalRecordService.getRecordsByPatientId(patientId));
        }

        if (authorizationService.isDoctor(authentication)) {
            Long doctorId = authorizationService.getCurrentDoctorId(authentication);
            if (doctorId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new AdminController.Message("ERROR", "Doctor account not found"));
            }
            return ResponseEntity.ok(medicalRecordService.getRecordsByDoctorAndPatientId(doctorId, patientId));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new AdminController.Message("ERROR", "Not authorized to view medical records"));
    }
}