package com.medicalsystem.controller;

import com.medicalsystem.service.AppointmentService;
import com.medicalsystem.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
public class PatientDashboardController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private MedicalRecordService medicalRecordService;

    @GetMapping("/{id}/dashboard-stats")
    public ResponseEntity<?> getDashboardStats(@PathVariable Long id) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("upcomingAppointments", appointmentService.getUpcomingAppointmentsCountForPatient(id));
        stats.put("totalAppointments", appointmentService.getTotalAppointmentsCountForPatient(id));
        stats.put("medicalRecordsCount", medicalRecordService.getRecordsByPatientId(id).size());
        return ResponseEntity.ok(stats);
    }
}
