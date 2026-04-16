package com.medicalsystem.appointments.repository;

import com.medicalsystem.appointments.model.Patient;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends MongoRepository<Patient, Long> {
    Optional<Patient> findByEmail(String email);
}
