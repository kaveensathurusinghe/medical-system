package com.medicalsystem.service;

import com.medicalsystem.model.MedicalRecord;
import com.medicalsystem.repository.MedicalRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MedicalRecordService {

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    public MedicalRecord createRecord(MedicalRecord medicalRecord) {
        if (medicalRecord.getId() == null) {
            medicalRecord.setId(sequenceGeneratorService.generateSequence("medical_record_seq"));
        }
        return medicalRecordRepository.save(medicalRecord);
    }

    public List<MedicalRecord> getAllRecords() {
        return medicalRecordRepository.findAll();
    }

    public Optional<MedicalRecord> getRecordById(Long id) {
        return medicalRecordRepository.findById(id);
    }

    public Optional<MedicalRecord> getRecordByAppointmentId(Long appointmentId) {
        return medicalRecordRepository.findByAppointmentId(appointmentId);
    }

    public List<MedicalRecord> getRecordsByPatientId(Long patientId) {
        return medicalRecordRepository.findByPatientId(patientId);
    }

    public List<MedicalRecord> getRecordsByDoctorId(Long doctorId) {
        return medicalRecordRepository.findByDoctorId(doctorId);
    }

    public List<MedicalRecord> getRecordsByDoctorAndPatientId(Long doctorId, Long patientId) {
        return medicalRecordRepository.findByDoctorIdAndPatientId(doctorId, patientId);
    }

    public long countRecords() {
        return medicalRecordRepository.count();
    }

    public long getTodayRecordsCount() {
        LocalDate today = LocalDate.now();
        return medicalRecordRepository.findAll().stream()
                .filter(record -> record.getRecordDate() != null)
                .filter(record -> today.equals(record.getRecordDate().toLocalDate()))
                .count();
    }
}