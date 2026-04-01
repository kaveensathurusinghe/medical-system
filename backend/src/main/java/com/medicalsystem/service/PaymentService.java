package com.medicalsystem.service;

import com.medicalsystem.model.Payment;
import com.medicalsystem.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment processPayment(String appointmentId, String patientId, double amount,
                                  String paymentMethod, String cardNumber,
                                  String cardHolderName, String expiryDate, String cvv) {
        Payment payment = new Payment();
        payment.setAppointmentId(appointmentId);
        payment.setPatientId(patientId);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setCardNumber(cardNumber);
        payment.setCardHolderName(cardHolderName);
        payment.setExpiryDate(expiryDate);
        payment.setCvv(cvv);
        payment.setStatus("COMPLETED");

        return paymentRepository.save(payment);
    }

    public List<Payment> getByPatientId(String patientId) {
        return paymentRepository.findByPatientId(patientId);
    }

    public List<Payment> getPaymentsByPatientId(String patientId) {
        return paymentRepository.findByPatientId(patientId);
    }

    public double getTotal(){
        return paymentRepository.calculateTotalPayments();
    }

    public List<Payment> getRecentPayments(int limit) {
        return paymentRepository.findRecentPayments(limit);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
}