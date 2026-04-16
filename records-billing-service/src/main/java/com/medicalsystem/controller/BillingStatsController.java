package com.medicalsystem.controller;

import com.medicalsystem.model.Payment;
import com.medicalsystem.service.AuthorizationService;
import com.medicalsystem.service.MedicalRecordService;
import com.medicalsystem.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class BillingStatsController {

    private final PaymentService paymentService;
    private final MedicalRecordService medicalRecordService;
    private final AuthorizationService authorizationService;

    public BillingStatsController(PaymentService paymentService,
                                  MedicalRecordService medicalRecordService,
                                  AuthorizationService authorizationService) {
        this.paymentService = paymentService;
        this.medicalRecordService = medicalRecordService;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/admin/payments")
    public ResponseEntity<?> getAllPayments(Authentication authentication) {
        if (!authorizationService.isAdmin(authentication)) {
            return forbiddenAdmin();
        }
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/admin/recent-payments")
    public ResponseEntity<?> getRecentPayments(@RequestParam(defaultValue = "5") int limit,
                                               Authentication authentication) {
        if (!authorizationService.isAdmin(authentication)) {
            return forbiddenAdmin();
        }
        return ResponseEntity.ok(paymentService.getRecentPayments(limit));
    }

    @GetMapping("/admin/payments/summary")
    public ResponseEntity<?> getPaymentsSummary(Authentication authentication) {
        if (!authorizationService.isAdmin(authentication)) {
            return forbiddenAdmin();
        }

        Map<String, Object> summary = Map.of(
                "paymentTotal", paymentService.getTotal(),
                "doctorChargesTotal", paymentService.getTotalDoctorCharges()
        );
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/admin/records/count")
    public ResponseEntity<?> getTotalRecords(Authentication authentication) {
        if (!authorizationService.isAdmin(authentication)) {
            return forbiddenAdmin();
        }
        return ResponseEntity.ok(Map.of("count", medicalRecordService.countRecords()));
    }

    @GetMapping("/public/payments/today-total")
    public ResponseEntity<?> getTodayPaymentsTotal() {
        return ResponseEntity.ok(Map.of("paymentTotal", paymentService.getTodayPaymentTotal()));
    }

    @GetMapping("/public/records/today-count")
    public ResponseEntity<?> getTodayRecordsCount() {
        return ResponseEntity.ok(Map.of("count", medicalRecordService.getTodayRecordsCount()));
    }

    private ResponseEntity<Message> forbiddenAdmin() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new Message("ERROR", "Only admin can access this endpoint"));
    }

    public static class Message {
        private String type;
        private String content;

        public Message(String type, String content) {
            this.type = type;
            this.content = content;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
