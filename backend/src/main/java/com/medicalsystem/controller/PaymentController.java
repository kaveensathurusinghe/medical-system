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

import java.util.List;

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
            return ResponseEntity.badRequest().body(new AdminController.Message("ERROR", "Invalid patient ID format"));
        }

        if (!authorizationService.canAccessPatient(authentication, patientIdLong)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AdminController.Message("ERROR", "You can only view your own payment history"));
        }

        return ResponseEntity.ok(paymentService.getPaymentsByPatientId(patientId));
    }
}
