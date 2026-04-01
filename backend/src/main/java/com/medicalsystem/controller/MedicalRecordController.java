package com.medicalsystem.controller;

import com.medicalsystem.dto.MedicalRecordRequest;
import com.medicalsystem.model.Appointment;
import com.medicalsystem.model.MedicalRecord;
import com.medicalsystem.service.AppointmentService;
import com.medicalsystem.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<?> createMedicalRecord(@RequestBody MedicalRecordRequest request) {
        Long appointmentId = Long.valueOf(request.getAppointmentId());
        Appointment appointment = appointmentService.getAppointmentById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        MedicalRecord record = new MedicalRecord();
        record.setAppointmentId(appointmentId);
        record.setPatientId(appointment.getPatientId());
        record.setDoctorId(appointment.getDoctorId());
        record.setRecordDate(LocalDateTime.now());
        record.setDiagnosis(request.getDiagnosis());
        record.setTreatment(request.getTreatment());
        record.setNotes(request.getNotes());

        medicalRecordService.createRecord(record);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<MedicalRecord>> getAllRecords() {
        return ResponseEntity.ok(medicalRecordService.getAllRecords());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicalRecord> getRecordById(@PathVariable Long id) {
        return medicalRecordService.getRecordById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicalRecord>> getRecordsByPatientId(@PathVariable Long patientId) {
        List<MedicalRecord> records = medicalRecordService.getRecordsByPatientId(patientId);
        return ResponseEntity.ok(records);
    }
}