package com.medicalsystem.controller;

import com.medicalsystem.service.AppointmentService;
import com.medicalsystem.service.AuthorizationService;
import com.medicalsystem.service.DoctorService;
import com.medicalsystem.service.MedicalRecordService;
import com.medicalsystem.service.PatientService;
import com.medicalsystem.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class DashboardController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private AuthorizationService authorizationService;

    @GetMapping("/dashboard-stats")
    public ResponseEntity<?> getDashboardStats(Authentication authentication) {
        if (!authorizationService.isAdmin(authentication)) {
            return forbiddenAdmin();
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("patientCount", patientService.countPatients());
        stats.put("doctorCount", doctorService.totalDoctors());
        stats.put("appointmentCount", appointmentService.getCompletedAppointmentsCount());
        stats.put("recordCount", medicalRecordService.countRecords());
        stats.put("paymentTotal", paymentService.getTotal());
        stats.put("doctorChargesTotal", paymentService.getTotalDoctorCharges());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/recent-appointments")
    public ResponseEntity<?> getRecentAppointments(Authentication authentication) {
        if (!authorizationService.isAdmin(authentication)) {
            return forbiddenAdmin();
        }

        return ResponseEntity.ok(appointmentService.getRecentAppointments(5));
    }

    @GetMapping("/recent-payments")
    public ResponseEntity<?> getRecentPayments(Authentication authentication) {
        if (!authorizationService.isAdmin(authentication)) {
            return forbiddenAdmin();
        }

        return ResponseEntity.ok(paymentService.getRecentPayments(5));
    }

    private ResponseEntity<AdminController.Message> forbiddenAdmin() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new AdminController.Message("ERROR", "Only admin can access this endpoint"));
    }
}