package com.medicalsystem.appointments.repository;

import com.medicalsystem.appointments.model.Doctor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorRepository extends MongoRepository<Doctor, Long> {
    Optional<Doctor> findByEmail(String email);
}
