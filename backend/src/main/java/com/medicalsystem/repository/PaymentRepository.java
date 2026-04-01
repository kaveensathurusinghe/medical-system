package com.medicalsystem.repository;

import com.medicalsystem.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {
    List<Payment> findByPatientId(String patientId);
    List<Payment> findAllByOrderByPaymentDateDesc();
}