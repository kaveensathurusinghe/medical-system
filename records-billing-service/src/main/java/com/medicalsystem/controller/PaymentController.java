package com.medicalsystem.controller;

import com.medicalsystem.model.Payment;
import com.medicalsystem.service.AuthorizationService;
import com.medicalsystem.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AuthorizationService authorizationService;

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getPaymentsByPatient(@PathVariable String patientId,
                                                  Authentication authentication) {
        Long patientIdLong;
        try {
            patientIdLong = Long.valueOf(patientId);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(new Message("ERROR", "Invalid patient ID format"));
        }

        if (!authorizationService.canAccessPatient(authentication, patientIdLong)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Message("ERROR", "You can only view your own payment history"));
        }

        return ResponseEntity.ok(paymentService.getPaymentsByPatientId(patientId));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getPaymentsByDoctor(@PathVariable String doctorId,
                                                 Authentication authentication) {
        Long doctorIdLong;
        try {
            doctorIdLong = Long.valueOf(doctorId);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(new Message("ERROR", "Invalid doctor ID format"));
        }

        if (!authorizationService.canAccessDoctor(authentication, doctorIdLong)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Message("ERROR", "You can only view your own payment data"));
        }

        return ResponseEntity.ok(paymentService.getPaymentsByDoctorId(doctorId));
    }

    @GetMapping("/doctor/{doctorId}/income")
    public ResponseEntity<?> getDoctorIncome(@PathVariable String doctorId,
                                             Authentication authentication) {
        Long doctorIdLong;
        try {
            doctorIdLong = Long.valueOf(doctorId);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(new Message("ERROR", "Invalid doctor ID format"));
        }

        if (!authorizationService.canAccessDoctor(authentication, doctorIdLong)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Message("ERROR", "You can only view your own income"));
        }

        List<Payment> doctorPayments = paymentService.getPaymentsByDoctorId(doctorId);
        Map<String, Object> response = new HashMap<>();
        response.put("doctorId", doctorId);
        response.put("paymentCount", doctorPayments.size());
        response.put("totalIncome", paymentService.getTotalIncomeByDoctorId(doctorId));
        return ResponseEntity.ok(response);
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
