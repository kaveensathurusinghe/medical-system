package com.medicalsystem.service;

import com.medicalsystem.model.Appointment;
import com.medicalsystem.model.Patient;
import com.medicalsystem.repository.AppointmentRepository;
import com.medicalsystem.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    /**
     * Register a new patient using the JPA-backed Patient entity.
     */
    public Patient registerPatient(String name, String email, String rawPassword,
                                   String phone, int age, String gender) {
        Patient patient = new Patient();
        patient.setId(sequenceGeneratorService.generateSequence("patient_seq"));
        patient.setName(name);
        patient.setEmail(email);
        patient.setPhone(phone);
        patient.setAge(age);
        patient.setGender(gender);
        patient.setPassword(passwordEncoder.encode(rawPassword));
        return patientRepository.save(patient);
    }

    public Optional<Patient> findByEmail(String email) {
        return patientRepository.findByEmail(email);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Patient savePatient(Patient patient) {
        if (patient.getId() == null) {
            patient.setId(sequenceGeneratorService.generateSequence("patient_seq"));
        }
        return patientRepository.save(patient);
    }

    public Optional<Patient> getPatientById(Long id) {
        return patientRepository.findById(id);
    }

    public void deletePatient(Long id) {
        patientRepository.deleteById(id);
    }

    public long countPatients() {
        return patientRepository.count();
    }

    /**
     * Count distinct patients who have at least one appointment with the given doctor.
     */
    public long countPatientsForDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId).stream()
                .map(Appointment::getPatientId)
                .distinct()
                .count();
    }
}