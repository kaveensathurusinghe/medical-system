package com.medicalsystem.repository;

import com.medicalsystem.model.MedicalRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordRepository extends MongoRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByPatientId(Long patientId);
}
