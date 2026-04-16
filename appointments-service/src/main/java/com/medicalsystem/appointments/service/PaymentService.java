package com.medicalsystem.appointments.service;

import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    public void processPayment(String appointmentId,
                               String patientId,
                               String doctorId,
                               double amount,
                               String paymentMethod,
                               String cardNumber,
                               String cardHolderName,
                               String expiryDate,
                               String cvv) {
        // no-op for now: microservice does not process payments, payment gateway remains external
        System.out.printf("[payments] processed (noop) appointment=%s patient=%s doctor=%s amount=%.2f\n",
                appointmentId, patientId, doctorId, amount);
    }
}
