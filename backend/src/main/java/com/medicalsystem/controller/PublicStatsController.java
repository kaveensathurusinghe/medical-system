package com.medicalsystem.controller;

import com.medicalsystem.service.AppointmentService;
import com.medicalsystem.service.MedicalRecordService;
import com.medicalsystem.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicStatsController {

    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;
    private final PaymentService paymentService;

    public PublicStatsController(AppointmentService appointmentService,
                                 MedicalRecordService medicalRecordService,
                                 PaymentService paymentService) {
        this.appointmentService = appointmentService;
        this.medicalRecordService = medicalRecordService;
        this.paymentService = paymentService;
    }

    @GetMapping("/today-at-a-glance")
    public ResponseEntity<?> getTodayAtAGlance() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("appointmentCount", appointmentService.getTodayAppointmentsCount());
        stats.put("recordCount", medicalRecordService.getTodayRecordsCount());
        stats.put("paymentTotal", paymentService.getTodayPaymentTotal());
        return ResponseEntity.ok(stats);
    }
}
